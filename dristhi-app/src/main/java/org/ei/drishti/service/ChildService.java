package org.ei.drishti.service;

import org.ei.drishti.AllConstants;
import org.ei.drishti.domain.Child;
import org.ei.drishti.domain.Mother;
import org.ei.drishti.domain.ServiceProvided;
import org.ei.drishti.domain.form.FormSubmission;
import org.ei.drishti.repository.*;
import org.ei.drishti.util.EasyMap;

import java.util.*;

import static org.ei.drishti.AllConstants.ChildRegistrationECFields.*;
import static org.ei.drishti.AllConstants.Immunizations.*;
import static org.ei.drishti.AllConstants.ChildIllnessFields.*;
import static org.ei.drishti.AllConstants.SPACE;
import static org.ei.drishti.domain.TimelineEvent.*;

public class ChildService {
    private AllBeneficiaries allBeneficiaries;
    private ChildRepository childRepository;
    private MotherRepository motherRepository;
    private AllTimelineEvents allTimelines;
    private ServiceProvidedService serviceProvidedService;
    private AllAlerts allAlerts;

    public ChildService(AllBeneficiaries allBeneficiaries, MotherRepository motherRepository, ChildRepository childRepository,
                        AllTimelineEvents allTimelineEvents, ServiceProvidedService serviceProvidedService, AllAlerts allAlerts) {
        this.allBeneficiaries = allBeneficiaries;
        this.childRepository = childRepository;
        this.motherRepository = motherRepository;
        this.allTimelines = allTimelineEvents;
        this.serviceProvidedService = serviceProvidedService;
        this.allAlerts = allAlerts;
    }

    public void register(FormSubmission submission) {
        Mother mother = motherRepository.findById(submission.entityId());
        List<Child> children = childRepository.findByMotherCaseId(mother.caseId());

        for (Child child : children) {
            childRepository.update(child.setIsClosed(false).setThayiCardNumber(mother.thayiCardNumber()).setDateOfBirth(mother.referenceDate()));
            allTimelines.add(forChildBirthInChildProfile(child.caseId(), mother.referenceDate(),
                    child.getDetail(AllConstants.ChildRegistrationFields.WEIGHT), child.getDetail(AllConstants.ChildRegistrationFields.IMMUNIZATIONS_GIVEN)));
            allTimelines.add(forChildBirthInMotherProfile(mother.caseId(), mother.referenceDate(), child.gender(), mother.referenceDate(), mother.getDetail(AllConstants.ChildRegistrationFields.DELIVERY_PLACE)));
            allTimelines.add(forChildBirthInECProfile(mother.ecCaseId(), mother.referenceDate(), child.gender(), mother.referenceDate()));
            String immunizationsGiven = child.getDetail(AllConstants.ChildRegistrationFields.IMMUNIZATIONS_GIVEN);
            for (String immunization : immunizationsGiven.split(SPACE)) {
                serviceProvidedService.add(ServiceProvided.forChildImmunization(child.caseId(), immunization, mother.referenceDate()));
            }
        }
    }

    public void registerForEC(FormSubmission submission) {
        Map<String, String> immunizationDateFieldMap = createImmunizationDateFieldMap();

        allTimelines.add(forChildBirthInChildProfile(
                submission.getFieldValue(AllConstants.ChildRegistrationFields.CHILD_ID),
                submission.getFieldValue(AllConstants.ChildRegistrationFields.DATE_OF_BIRTH),
                submission.getFieldValue(AllConstants.ChildRegistrationFields.WEIGHT),
                submission.getFieldValue(AllConstants.ChildRegistrationFields.IMMUNIZATIONS_GIVEN)));
        allTimelines.add(forChildBirthInMotherProfile(
                submission.getFieldValue(AllConstants.ChildRegistrationFields.MOTHER_ID),
                submission.getFieldValue(AllConstants.ChildRegistrationFields.DATE_OF_BIRTH),
                submission.getFieldValue(AllConstants.ChildRegistrationFields.GENDER),
                null, null));
        allTimelines.add(forChildBirthInECProfile(
                submission.entityId(),
                submission.getFieldValue(AllConstants.ChildRegistrationFields.DATE_OF_BIRTH),
                submission.getFieldValue(AllConstants.ChildRegistrationFields.GENDER), null));
        String immunizationsGiven = submission.getFieldValue(AllConstants.ChildRegistrationFields.IMMUNIZATIONS_GIVEN);
        for (String immunization : immunizationsGiven.split(SPACE)) {
            serviceProvidedService.add(ServiceProvided.forChildImmunization(submission.entityId(), immunization, submission.getFieldValue(immunizationDateFieldMap.get(immunization))));
        }
    }

    private Map<String, String> createImmunizationDateFieldMap() {
        Map<String, String> immunizationDateFieldsMap = new HashMap<String, String>();
        immunizationDateFieldsMap.put(BCG, BCG_DATE);

        immunizationDateFieldsMap.put(MEASLES, MEASLES_DATE);
        immunizationDateFieldsMap.put(MEASLES_BOOSTER, MEASLESBOOSTER_DATE);

        immunizationDateFieldsMap.put(OPV_0, OPV_0_DATE);
        immunizationDateFieldsMap.put(OPV_1, OPV_1_DATE);
        immunizationDateFieldsMap.put(OPV_2, OPV_2_DATE);
        immunizationDateFieldsMap.put(OPV_3, OPV_3_DATE);
        immunizationDateFieldsMap.put(OPV_BOOSTER, OPVBOOSTER_DATE);

        immunizationDateFieldsMap.put(DPT_BOOSTER_1, DPTBOOSTER_1_DATE);
        immunizationDateFieldsMap.put(DPT_BOOSTER_2, DPTBOOSTER_2_DATE);

        immunizationDateFieldsMap.put(HEPATITIS_BIRTH_DOSE, HEPB_BIRTH_DOSE_DATE);

        immunizationDateFieldsMap.put(PENTAVALENT_1, PENTAVALENT_1_DATE);
        immunizationDateFieldsMap.put(PENTAVALENT_2, PENTAVALENT_2_DATE);
        immunizationDateFieldsMap.put(PENTAVALENT_3, PENTAVALENT_3_DATE);

        immunizationDateFieldsMap.put(MMR, MMR_DATE);
        immunizationDateFieldsMap.put(JE, JE_DATE);

        return immunizationDateFieldsMap;
    }

    public void pncRegistrationOA(FormSubmission submission) {
        Mother mother = motherRepository.findAllCasesForEC(submission.entityId()).get(0);
        List<Child> children = childRepository.findByMotherCaseId(mother.caseId());

        for (Child child : children) {
            childRepository.update(child.setIsClosed(false).setThayiCardNumber(mother.thayiCardNumber()).setDateOfBirth(mother.referenceDate()));
            allTimelines.add(forChildBirthInChildProfile(child.caseId(), mother.referenceDate(),
                    child.getDetail(AllConstants.PNCRegistrationOAFields.WEIGHT), child.getDetail(AllConstants.PNCRegistrationOAFields.IMMUNIZATIONS_GIVEN)));
            allTimelines.add(forChildBirthInMotherProfile(mother.caseId(), mother.referenceDate(), child.gender(),
                    mother.referenceDate(), submission.getFieldValue(AllConstants.PNCRegistrationOAFields.DELIVERY_PLACE)));
            allTimelines.add(forChildBirthInECProfile(mother.ecCaseId(), mother.referenceDate(), child.gender(),
                    mother.referenceDate()));
            String immunizationsGiven = child.getDetail(AllConstants.PNCRegistrationOAFields.IMMUNIZATIONS_GIVEN);
            for (String immunization : immunizationsGiven.split(SPACE)) {
                serviceProvidedService.add(ServiceProvided.forChildImmunization(child.caseId(), immunization, mother.referenceDate()));
            }
        }
    }

    public void updateImmunizations(FormSubmission submission) {
        String immunizationDate = submission.getFieldValue(AllConstants.ChildImmunizationsFields.IMMUNIZATION_DATE);
        List<String> immunizationsGivenList = splitFieldValueBySpace(submission, AllConstants.ChildImmunizationsFields.IMMUNIZATIONS_GIVEN);
        List<String> previousImmunizationsList = splitFieldValueBySpace(submission, AllConstants.ChildImmunizationsFields.PREVIOUS_IMMUNIZATIONS_GIVEN);
        immunizationsGivenList.removeAll(previousImmunizationsList);
        for (String immunization : immunizationsGivenList) {
            allTimelines.add(forChildImmunization(submission.entityId(), immunization, immunizationDate));
            serviceProvidedService.add(ServiceProvided.forChildImmunization(submission.entityId(), immunization, immunizationDate));
            allAlerts.changeAlertStatusToInProcess(submission.entityId(), immunization);
        }
    }

    private ArrayList<String> splitFieldValueBySpace(FormSubmission submission, String fieldName) {
        return new ArrayList<String>(Arrays.asList(submission.getFieldValue(fieldName).split(SPACE)));
    }

    public void pncVisitHappened(FormSubmission submission) {
        List<Child> children = childRepository.findByMotherCaseId(submission.entityId());
        for (Child child : children) {
            allTimelines.add(forChildPNCVisit(child.caseId(), submission.getFieldValue(AllConstants.PNCVisitFields.PNC_VISIT_DAY),
                    submission.getFieldValue(AllConstants.PNCVisitFields.PNC_VISIT_DATE), child.getDetail(AllConstants.PNCVisitFields.WEIGHT), child.getDetail(AllConstants.PNCVisitFields.TEMPERATURE)));
            serviceProvidedService.add(
                    ServiceProvided.forChildPNCVisit(
                            child.caseId(),
                            submission.getFieldValue(AllConstants.PNCVisitFields.PNC_VISIT_DAY),
                            submission.getFieldValue(AllConstants.PNCVisitFields.PNC_VISIT_DATE)));
        }
    }

    public void close(FormSubmission submission) {
        allBeneficiaries.closeChild(submission.entityId());
    }

    public void updatePhotoPath(String entityId, String imagePath) {
        childRepository.updatePhotoPath(entityId, imagePath);
    }

    public void updateIllnessStatus(FormSubmission submission) {
        serviceProvidedService.add(
                ServiceProvided.forChildIllnessVisit(submission.entityId(),
                        submission.getFieldValue("submissionDate"),
                        createChildIllnessMap(submission))
        );
    }

    private Map<String, String> createChildIllnessMap(FormSubmission submission) {
        return EasyMap.create(CHILD_SIGNS, submission.getFieldValue(CHILD_SIGNS))
                .put(CHILD_SIGNS_OTHER, submission.getFieldValue(CHILD_SIGNS_OTHER))
                .put(SICK_VISIT_DATE, submission.getFieldValue(SICK_VISIT_DATE))
                .put(REPORT_CHILD_DISEASE, submission.getFieldValue(REPORT_CHILD_DISEASE))
                .put(REPORT_CHILD_DISEASE_OTHER, submission.getFieldValue(REPORT_CHILD_DISEASE_OTHER))
                .put(REPORT_CHILD_DISEASE_DATE, submission.getFieldValue(REPORT_CHILD_DISEASE_DATE))
                .put(REPORT_CHILD_DISEASE_PLACE, submission.getFieldValue(REPORT_CHILD_DISEASE_PLACE))
                .put(CHILD_REFERRAL, submission.getFieldValue(CHILD_REFERRAL))
                .map();
    }
}
