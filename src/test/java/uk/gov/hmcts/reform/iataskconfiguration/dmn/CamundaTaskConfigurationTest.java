package uk.gov.hmcts.reform.iataskconfiguration.dmn;

import lombok.Builder;
import lombok.Value;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableImpl;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.util.StringUtils;
import uk.gov.hmcts.reform.iataskconfiguration.DmnDecisionTableBaseUnitTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.iataskconfiguration.DmnDecisionTable.WA_TASK_CONFIGURATION_IA_ASYLUM;

class CamundaTaskConfigurationTest extends DmnDecisionTableBaseUnitTest {

    @BeforeAll
    public static void initialization() {
        CURRENT_DMN_DECISION_TABLE = WA_TASK_CONFIGURATION_IA_ASYLUM;
    }

    @ParameterizedTest
    @CsvSource({
        "followUpNoticeOfChange", "adaFollowUpNoticeOfChange"
    })
    void when_taskId_then_return_Access_Requests(String taskType) {
        VariableMap inputVariables = new VariableMapImpl();

        inputVariables.putValue("taskAttributes", Map.of("taskType", taskType));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        List<Map<String, Object>> workTypeResultList = dmnDecisionTableResult.getResultList().stream()
            .filter((r) -> r.containsValue("workType"))
            .collect(Collectors.toList());

        assertEquals(1, workTypeResultList.size());

        assertEquals(Map.of(
            "name", "workType",
            "value", "access_requests"
        ), workTypeResultList.get(0));
    }

    @Test
    void if_this_test_fails_needs_updating_with_your_changes() {
        //The purpose of this test is to prevent adding new rows without being tested
        DmnDecisionTableImpl logic = (DmnDecisionTableImpl) decision.getDecisionLogic();
        assertThat(logic.getRules().size(), is(17));
    }

    @SuppressWarnings("checkstyle:indentation")
    @ParameterizedTest
    @CsvSource(value = {
        "refusalOfHumanRights, Human rights",
        "refusalOfEu, EEA",
        "deprivation, DoC",
        "protection, Protection",
        "revocationOfProtection, Revocation",
        "NULL_VALUE, ''",
        "'', ''"
    }, nullValues = "NULL_VALUE")
    void when_caseData_then_return_expected_appealType(String appealType, String expectedAppealType) {
        VariableMap inputVariables = new VariableMapImpl();
        Map<String, Object> caseData = new HashMap<>(); // allow null values
        caseData.put("appealType", appealType);
        inputVariables.putValue("caseData", caseData);

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        assertTrue(dmnDecisionTableResult.getResultList().contains(Map.of(
            "name", "appealType",
            "value", expectedAppealType
        )));

        assertTrue(dmnDecisionTableResult.getResultList().contains(Map.of(
            "name", "caseManagementCategory",
            "value", expectedAppealType
        )));
    }

    @Test
    void when_caseData_then_return_expected_case_management_category() {
        String refusalOfEuLabel = "Refusal of application under the EEA regulations";
        String revocationLabel = "Revocation of a protection status";
        List<Map<String, Object>> caseManagementCategories = List.of(
            Map.of(
                "value",
                Map.of("code", "refusalOfHumanRights", "label", "Refusal of a human rights claim"),
                "list_items",
                List.of(Map.of("code", "refusalOfHumanRights", "label", "Refusal of a human rights claim"))
            ),
            Map.of(
                "value", Map.of("code", "refusalOfEu", "label", "Refusal of application under the EEA regulations"),
                "list_items", List.of(Map.of("code", "refusalOfEu", "label", refusalOfEuLabel))
            ),
            Map.of(
                "value", Map.of("code", "deprivation", "label", "Deprivation of citizenship"),
                "list_items", List.of(Map.of("code", "deprivation", "label", "Deprivation of citizenship"))
            ),
            Map.of(
                "value", Map.of("code", "protection", "label", "Refusal of protection claim"),
                "list_items", List.of(Map.of("code", "protection", "label", "Refusal of protection claim"))
            ),
            Map.of(
                "value", Map.of("code", "revocationOfProtection", "label", "Revocation of a protection status"),
                "list_items", List.of(Map.of("code", "revocationOfProtection", "label", revocationLabel))
            )
        );

        List<String> expectedCaseManagementCategories = List.of(
            "Human rights",
            "EEA",
            "DoC",
            "Protection",
            "Revocation"
        );

        for (int i = 0; i < caseManagementCategories.size(); i++) {
            Map<String, Object> caseManagementCategory = caseManagementCategories.get(i);
            VariableMap inputVariables = new VariableMapImpl();
            Map<String, Object> caseData = new HashMap<>(); // allow null values
            caseData.put("caseManagementCategory", caseManagementCategory);
            inputVariables.putValue("caseData", caseData);

            DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

            assertTrue(dmnDecisionTableResult.getResultList().contains(Map.of(
                "name", "caseManagementCategory",
                "value", expectedCaseManagementCategories.get(i)
            )));
        }
    }

    public static Stream<Arguments> workTypeScenarioProvider() {
        List<Map<String, String>> routineWork = List.of(Map.of(
            "name", "workType",
            "value", "routine_work"
        ));
        List<Map<String, String>> decisionMakingWork = List.of(Map.of(
            "name", "workType",
            "value", "decision_making_work"
        ));
        List<Map<String, String>> hearingWork = List.of(Map.of(
            "name", "workType",
            "value", "hearing_work"
        ));
        List<Map<String, String>> applications = List.of(Map.of(
            "name", "workType",
            "value", "applications"
        ));
        List<Map<String, String>> upperTribunal = List.of(Map.of(
            "name", "workType",
            "value", "upper_tribunal"
        ));

        return Stream.of(
            Arguments.of("arrangeOfflinePayment", routineWork),
            Arguments.of("markCaseAsPaid", routineWork),
            Arguments.of("attendCma", routineWork),
            Arguments.of("caseSummaryHearingBundleStartDecision", routineWork),
            Arguments.of("adaCaseSummaryHearingBundleStartDecision", routineWork),
            Arguments.of("followUpExtendedDirection", routineWork),
            Arguments.of("adaFollowUpExtendedDirection", routineWork),
            Arguments.of("followUpNonStandardDirection", routineWork),
            Arguments.of("adaFollowUpNonStandardDirection", routineWork),
            Arguments.of("reviewClarifyingQuestionsAnswers", routineWork),
            Arguments.of("reviewCaseMarkedUnsuitableForADA", routineWork),
            Arguments.of("reviewCaseTransferredOutOfADA", routineWork),
            Arguments.of("adaListCase", routineWork),
            Arguments.of("reviewAdditionalEvidence", decisionMakingWork),
            Arguments.of("adaReviewAdditionalEvidence", decisionMakingWork),
            Arguments.of("reviewTheAppeal", decisionMakingWork),
            Arguments.of("adaReviewTheAppeal", decisionMakingWork),
            Arguments.of("followUpOverdueRespondentEvidence", decisionMakingWork),
            Arguments.of("adaFollowUpOverdueRespondentEvidence", decisionMakingWork),
            Arguments.of("reviewRespondentEvidence", decisionMakingWork),
            Arguments.of("adaReviewRespondentEvidence", decisionMakingWork),
            Arguments.of("followUpOverdueCaseBuilding", decisionMakingWork),
            Arguments.of("adaFollowUpOverdueCaseBuilding", decisionMakingWork),
            Arguments.of("reviewAppealSkeletonArgument", decisionMakingWork),
            Arguments.of("adaReviewAppealSkeletonArgument", decisionMakingWork),
            Arguments.of("followUpOverdueReasonsForAppeal", decisionMakingWork),
            Arguments.of("reviewReasonsForAppeal", decisionMakingWork),
            Arguments.of("followUpOverdueClarifyingAnswers", decisionMakingWork),
            Arguments.of("reviewClarifyingAnswers", decisionMakingWork),
            Arguments.of("followUpOverdueRespondentReview", decisionMakingWork),
            Arguments.of("adaFollowUpOverdueRespondentReview", decisionMakingWork),
            Arguments.of("reviewRespondentResponse", decisionMakingWork),
            Arguments.of("followUpOverdueCMARequirements", decisionMakingWork),
            Arguments.of("reviewCmaRequirements", decisionMakingWork),
            Arguments.of("reviewAdditionalHomeOfficeEvidence", decisionMakingWork),
            Arguments.of("adaReviewAdditionalHomeOfficeEvidence", decisionMakingWork),
            Arguments.of("reviewAdditionalAppellantEvidence", decisionMakingWork),
            Arguments.of("reviewAddendumHomeOfficeEvidence", decisionMakingWork),
            Arguments.of("adaReviewAddendumEvidence", decisionMakingWork),
            Arguments.of("decideOnTimeExtension", decisionMakingWork),
            Arguments.of("sendDecisionsAndReasons", decisionMakingWork),
            Arguments.of("adaSendDecisionsAndReasons", decisionMakingWork),
            Arguments.of("reviewADASuitability", decisionMakingWork),
            Arguments.of("reviewHearingBundle", hearingWork),
            Arguments.of("generateDraftDecisionAndReasons", hearingWork),
            Arguments.of("uploadDecision", hearingWork),
            Arguments.of("uploadHearingRecording", hearingWork),
            Arguments.of("editListing", hearingWork),
            Arguments.of("adaEditListing", hearingWork),
            Arguments.of("followUpOverdueHearingRequirements", hearingWork),
            Arguments.of("reviewHearingRequirements", hearingWork),
            Arguments.of("allocateHearingJudge", hearingWork),
            Arguments.of("adaAllocateHearingJudge", hearingWork),
            Arguments.of("prepareDecisionsAndReasons", hearingWork),
            Arguments.of("adaPrepareDecisionsAndReasons", hearingWork),
            Arguments.of("startDecisionsAndReasonsDocument", hearingWork),
            Arguments.of("createHearingBundle", hearingWork),
            Arguments.of("createCaseSummary", hearingWork),
            Arguments.of("processApplication", applications),
            Arguments.of("adaProcessApplicationToUpdateHearingRequirements", applications),
            Arguments.of("adaProcessApplicationToUpdateAppealDetails", applications),
            Arguments.of("adaProcessApplicationToReinstateAnEndedAppeal", applications),
            Arguments.of("adaProcessApplicationToOther", applications),
            Arguments.of("adaLinkUnlinkAppeals", applications),
            Arguments.of("processHearingRequirementsApplication", applications),
            Arguments.of("processHearingCentreApplication", applications),
            Arguments.of("processApplicationToExpedite", applications),
            Arguments.of("processApplicationToTransfer", applications),
            Arguments.of("processApplicationForTimeExtension", applications),
            Arguments.of("processApplicationToWithdraw", applications),
            Arguments.of("processAppealDetailsApplication", applications),
            Arguments.of("processReinstatementApplication", applications),
            Arguments.of("processApplicationToReviewDecision", applications),
            Arguments.of("adaProcessApplicationToAdjourn", applications),
            Arguments.of("adaProcessApplicationToExpedite", applications),
            Arguments.of("adaProcessApplicationForTimeExtension", applications),
            Arguments.of("adaProcessApplicationToWithdraw", applications),
            Arguments.of("adaProcessApplicationToReviewDecision", applications),
            Arguments.of("decideAnFTPA", upperTribunal),
            Arguments.of("adaDecideAnFTPA", upperTribunal)
        );
    }

    @ParameterizedTest
    @CsvSource({
        "reviewSpecificAccessRequestJudiciary", "reviewSpecificAccessRequestLegalOps",
        "reviewSpecificAccessRequestAdmin","reviewSpecificAccessRequestCTSC"
    })
    void when_taskId_then_return_Access_requests(String taskType) {
        VariableMap inputVariables = new VariableMapImpl();

        String roleAssignmentId = UUID.randomUUID().toString();
        Map<String, String> taskAttributes = Map.of("taskType", taskType, "roleAssignmentId", roleAssignmentId);
        inputVariables.putValue("taskAttributes", taskAttributes);

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        List<Map<String, Object>> workTypeResultList = dmnDecisionTableResult.getResultList().stream()
            .filter((r) -> r.containsValue("workType"))
            .collect(Collectors.toList());

        assertEquals(1, workTypeResultList.size());

        assertEquals(Map.of(
            "name", "workType",
            "value", "access_requests"
        ), workTypeResultList.get(0));
    }

    @ParameterizedTest
    @CsvSource({
        "reviewSpecificAccessRequestJudiciary", "reviewSpecificAccessRequestLegalOps",
        "reviewSpecificAccessRequestAdmin","reviewSpecificAccessRequestCTSC"
    })
    void should_return_request_value_when_role_assignment_id_exists_in_task_attributes(String taskType) {
        VariableMap inputVariables = new VariableMapImpl();

        String roleAssignmentId = UUID.randomUUID().toString();

        Map<String, String> taskAttributes = Map.of("taskType", taskType, "roleAssignmentId", roleAssignmentId);
        inputVariables.putValue("taskAttributes", taskAttributes);

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        List<Map<String, Object>> dmnResults = dmnDecisionTableResult.getResultList().stream()
            .filter((r) -> r.containsValue("additionalProperties_roleAssignmentId"))
            .collect(Collectors.toList());

        assertThat(dmnResults.size(), is(1));

        assertTrue(dmnResults.contains(Map.of(
            "name", "additionalProperties_roleAssignmentId",
            "value", roleAssignmentId
        )));

    }

    @ParameterizedTest
    @MethodSource("workTypeScenarioProvider")
    void when_taskId_then_return_workType(String taskType, List<Map<String, String>> expected) {
        VariableMap inputVariables = new VariableMapImpl();

        inputVariables.putValue("taskAttributes", Map.of("taskType", taskType));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        List<Map<String, Object>> workTypeResultList = dmnDecisionTableResult.getResultList().stream()
            .filter((r) -> r.containsValue("workType"))
            .collect(Collectors.toList());

        assertEquals(expected, workTypeResultList);
    }

    @ParameterizedTest
    @CsvSource({
        "reviewHearingBundle", "generateDraftDecisionAndReasons", "uploadDecision", "reviewAddendumHomeOfficeEvidence",
        "reviewAddendumAppellantEvidence", "reviewAddendumEvidence",
        "adaReviewAddendumEvidence", "reviewSpecificAccessRequestJudiciary",
        "reviewSpecificAccessRequestLegalOps", "reviewSpecificAccessRequestAdmin","reviewSpecificAccessRequestCTSC",
        "processApplicationToReviewDecision", "adaProcessApplicationToAdjourn", "adaProcessApplicationToExpedite",
        "adaProcessApplicationForTimeExtension", "adaProcessApplicationToWithdraw",
        "adaProcessApplicationToReviewDecision", "sendDecisionsAndReasons", "adaSendDecisionsAndReasons",
        "prepareDecisionsAndReasons", "adaPrepareDecisionsAndReasons", "decideAnFTPA", "adaDecideAnFTPA",
        "reviewADASuitability"
    })
    void when_taskId_then_return_judicial_role_category(String taskType) {
        VariableMap inputVariables = new VariableMapImpl();

        String roleAssignmentId = UUID.randomUUID().toString();
        inputVariables.putValue("taskAttributes", Map.of(
                                    "taskType", taskType,
                                    "additionalProperties", Map.of("roleAssignmentId", roleAssignmentId)
                                )
        );

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        List<Map<String, Object>> workTypeResultList = dmnDecisionTableResult.getResultList().stream()
            .filter((r) -> r.containsValue("roleCategory"))
            .collect(Collectors.toList());

        assertEquals(1, workTypeResultList.size());

        assertEquals(Map.of(
            "name", "roleCategory",
            "value", "JUDICIAL"
        ), workTypeResultList.get(0));
    }

    @ParameterizedTest
    @CsvSource({
        "arrangeOfflinePayment", "markCaseAsPaid", "allocateHearingJudge", "uploadHearingRecording", "editListing",
        "adaListCase", "adaEditListing", "adaAllocateHearingJudge"
    })
    void when_taskId_then_return_Admin_role_category(String taskType) {
        VariableMap inputVariables = new VariableMapImpl();

        inputVariables.putValue("taskAttributes", Map.of("taskType", taskType));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        List<Map<String, Object>> workTypeResultList = dmnDecisionTableResult.getResultList().stream()
            .filter((r) -> r.containsValue("roleCategory"))
            .collect(Collectors.toList());

        assertEquals(1, workTypeResultList.size());

        assertEquals(Map.of(
            "name", "roleCategory",
            "value", "ADMIN"
        ), workTypeResultList.get(0));
    }

    @ParameterizedTest
    @CsvSource({
        "processApplication","adaProcessApplicationToUpdateHearingRequirements",
        "adaProcessApplicationToUpdateAppealDetails", "adaProcessApplicationToReinstateAnEndedAppeal",
        "adaProcessApplicationToOther", "adaLinkUnlinkAppeals", "reviewTheAppeal","adaReviewTheAppeal",
        "decideOnTimeExtension", "reviewRespondentEvidence", "adaReviewRespondentEvidence", "reviewReasonsForAppeal",
        "reviewAppealSkeletonArgument", "adaReviewAppealSkeletonArgument",
        "reviewClarifyingQuestionsAnswers", "reviewCmaRequirements", "attendCma", "reviewRespondentResponse",
        "caseSummaryHearingBundleStartDecision", "adaCaseSummaryHearingBundleStartDecision",
        "reviewHearingRequirements", "followUpOverdueRespondentEvidence",
        "adaFollowUpOverdueRespondentEvidence", "adaFollowUpOverdueCaseBuilding", "adaFollowUpOverdueRespondentReview",
        "adaFollowUpNonStandardDirection", "adaFollowUpNoticeOfChange",
        "followUpOverdueCaseBuilding", "followUpOverdueReasonsForAppeal", "followUpOverdueClarifyingAnswers",
        "followUpOverdueCmaRequirements", "followUpOverdueRespondentReview", "followUpOverdueHearingRequirements",
        "followUpNonStandardDirection", "followUpNoticeOfChange", "reviewAdditionalEvidence",
        "reviewAdditionalHomeOfficeEvidence","followUpExtendedDirection", "adaFollowUpExtendedDirection",
        "adaReviewAdditionalEvidence", "adaReviewAdditionalHomeOfficeEvidence","reviewCaseTransferredOutOfADA",
        "reviewCaseMarkedUnsuitableForADA"
    })
    void when_taskId_then_return_legal_operations_role_category(String taskType) {
        VariableMap inputVariables = new VariableMapImpl();

        inputVariables.putValue("taskAttributes", Map.of("taskType", taskType));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        List<Map<String, Object>> workTypeResultList = dmnDecisionTableResult.getResultList().stream()
            .filter((r) -> r.containsValue("roleCategory"))
            .collect(Collectors.toList());

        assertEquals(1, workTypeResultList.size());

        assertEquals(Map.of(
            "name", "roleCategory",
            "value", "LEGAL_OPERATIONS"
        ), workTypeResultList.get(0));
    }

    @ParameterizedTest
    @MethodSource("nameAndValueScenarioProvider")
    void when_caseData_and_taskType_then_return_expected_name_and_value_rows(Scenario scenario) {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", scenario.caseData);
        inputVariables.putValue("taskAttributes", scenario.getTaskAttributes());

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        assertThat(dmnDecisionTableResult.getResultList(), is(getExpectedValues(scenario)));
    }

    private static Stream<Scenario> nameAndValueScenarioProvider() {
        Scenario givenCaseDataIsMissedThenDefaultToTaylorHouseScenario = Scenario.builder()
            .caseData(emptyMap())
            .expectedCaseNameValue(null)
            .expectedAppealTypeValue("")
            .expectedRegionValue("1")
            .expectedLocationValue("765324")
            .expectedLocationNameValue("Taylor House")
            .expectedCaseManagementCategoryValue("")
            .expectedDescriptionValue("")
            .expectedReconfigureValue("true")
            .build();

        String refusalOfEuLabel = "Refusal of a human rights claim";
        Scenario givenCaseDataIsPresentThenReturnNameAndValueScenario = Scenario.builder()
            .caseData(Map.of(
                "appealType", "refusalOfHumanRights",
                "appellantGivenNames", "some appellant given names",
                "appellantFamilyName", "some appellant family name",
                "caseManagementLocation", Map.of(
                    "region", "some other region",
                    "baseLocation", "some other location"
                ),
                "staffLocation", "some other location name",
                "caseManagementCategory", Map.of(
                    "value", Map.of("code", "refusalOfHumanRights", "label", "Refusal of a human rights claim"),
                    "list_items", List.of(Map.of("code", "refusalOfHumanRights", "label", refusalOfEuLabel))
                )
            ))
            .taskAttributes(Map.of("taskType", "markCaseAsPaid"))
            .expectedCaseNameValue("some appellant given names some appellant family name")
            .expectedAppealTypeValue("Human rights")
            .expectedRegionValue("some other region")
            .expectedLocationValue("some other location")
            .expectedLocationNameValue("some other location name")
            .expectedCaseManagementCategoryValue("Human rights")
            .expectedWorkType("routine_work")
            .expectedReconfigureValue("true")
            .expectedRoleCategory("ADMIN")
            .expectedDescriptionValue(
                "[Mark the appeal as paid](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/markAppealPaid)")
            .build();

        Scenario givenSomeCaseDataAndArrangeOfflinePaymentTaskIdThenReturnExpectedNameAndValueScenario =
            Scenario.builder()
                .caseData(Map.of(
                    "appealType", "refusalOfHumanRights",
                    "appellantGivenNames", "some appellant given names",
                    "appellantFamilyName", "some appellant family name",
                    "caseManagementLocation", Map.of(
                        "region", "some other region",
                        "baseLocation", "some other location"
                    ),
                    "staffLocation", "some other location name",
                    "caseManagementCategory", Map.of(
                        "value", Map.of(
                            "code", "refusalOfHumanRights",
                            "label", "Refusal of a human rights claim"
                        ),
                        "list_items", List.of(Map.of(
                            "code", "refusalOfHumanRights",
                            "label", refusalOfEuLabel
                        ))
                    )
                ))
                .taskAttributes(Map.of("taskType", "markCaseAsPaid"))
                .expectedCaseNameValue("some appellant given names some appellant family name")
                .expectedAppealTypeValue("Human rights")
                .expectedRegionValue("some other region")
                .expectedLocationValue("some other location")
                .expectedLocationNameValue("some other location name")
                .expectedCaseManagementCategoryValue("Human rights")
                .expectedReconfigureValue("true")
                .expectedWorkType("routine_work")
                .expectedRoleCategory("ADMIN")
                .expectedDescriptionValue("[Mark the appeal as "
                                              + "paid](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/markAppealPaid)")
                .build();

        Scenario givenSomeCaseDataAndTaskTypeIsEmptyThenExpectNoWorkTypeRuleScenario =
            Scenario.builder()
                .caseData(Map.of(
                    "appealType", "refusalOfHumanRights",
                    "appellantGivenNames", "some appellant given names",
                    "appellantFamilyName", "some appellant family name",
                    "caseManagementLocation", Map.of(
                        "region", "some other region",
                        "baseLocation", "some other location"
                    ),
                    "staffLocation", "some other location name",
                    "caseManagementCategory", Map.of(
                        "value", Map.of(
                            "code", "refusalOfHumanRights",
                            "label", "Refusal of a human rights claim"
                        ),
                        "list_items", List.of(Map.of(
                            "code", "refusalOfHumanRights",
                            "label", refusalOfEuLabel
                        ))
                    )
                ))
                .taskAttributes(Map.of("taskType", "markCaseAsPaid"))
                .expectedCaseNameValue("some appellant given names some appellant family name")
                .expectedAppealTypeValue("Human rights")
                .expectedRegionValue("some other region")
                .expectedLocationValue("some other location")
                .expectedLocationNameValue("some other location name")
                .expectedCaseManagementCategoryValue("Human rights")
                .expectedWorkType("routine_work")
                .expectedReconfigureValue("true")
                .expectedRoleCategory("ADMIN")
                .expectedDescriptionValue("[Mark the appeal as "
                                              + "paid](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/markAppealPaid)")
                .build();

        Scenario givenNoCaseDataAndSomeTaskTypeThenExpectOnlyTheWorkTypeRuleScenario =
            Scenario.builder()
                .caseData(emptyMap())
                .taskAttributes(Map.of("taskType", "markCaseAsPaid"))
                .expectedCaseNameValue(null)
                .expectedAppealTypeValue("")
                .expectedRegionValue("1")
                .expectedLocationValue("765324")
                .expectedLocationNameValue("Taylor House")
                .expectedCaseManagementCategoryValue("")
                .expectedWorkType("routine_work")
                .expectedReconfigureValue("true")
                .expectedRoleCategory("ADMIN")
                .expectedDescriptionValue("[Mark the appeal as "
                                              + "paid](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/markAppealPaid)")
                .build();

        return Stream.of(
            givenCaseDataIsMissedThenDefaultToTaylorHouseScenario,
            givenCaseDataIsPresentThenReturnNameAndValueScenario,
            givenSomeCaseDataAndArrangeOfflinePaymentTaskIdThenReturnExpectedNameAndValueScenario,
            givenSomeCaseDataAndTaskTypeIsEmptyThenExpectNoWorkTypeRuleScenario,
            givenNoCaseDataAndSomeTaskTypeThenExpectOnlyTheWorkTypeRuleScenario
        );
    }

    @Value
    @Builder
    private static class Scenario {
        Map<String, Object> caseData;
        Map<String, Object> taskAttributes;
        String expectedCaseNameValue;
        String expectedAppealTypeValue;
        String expectedRegionValue;
        String expectedLocationValue;
        String expectedLocationNameValue;
        String expectedCaseManagementCategoryValue;
        String expectedWorkType;
        String expectedRoleCategory;
        String expectedDescriptionValue;
        String expectedReconfigureValue;
    }

    private List<Map<String, Object>> getExpectedValues(Scenario scenario) {
        List<Map<String, Object>> rules = new ArrayList<>();

        getExpectedValueWithReconfigure(
            rules,
            "caseName",
            scenario.getExpectedCaseNameValue(),
            scenario.getExpectedReconfigureValue()
        );
        getExpectedValue(rules, "appealType", scenario.getExpectedAppealTypeValue());
        getExpectedValue(rules, "region", scenario.getExpectedRegionValue());
        getExpectedValueWithReconfigure(
            rules,
            "location",
            scenario.getExpectedLocationValue(),
            scenario.getExpectedReconfigureValue()
        );
        getExpectedValueWithReconfigure(
            rules,
            "locationName",
            scenario.getExpectedLocationNameValue(),
            scenario.getExpectedReconfigureValue()
        );
        getExpectedValue(rules, "caseManagementCategory", scenario.getExpectedCaseManagementCategoryValue());
        if (!Objects.isNull(scenario.getTaskAttributes())
            && StringUtils.isNotBlank(scenario.taskAttributes.get("taskType").toString())) {
            getExpectedValue(rules, "workType", scenario.getExpectedWorkType());
            getExpectedValue(rules, "roleCategory", scenario.getExpectedRoleCategory());
        }

        getExpectedValue(rules, "description", scenario.getExpectedDescriptionValue());
        return rules;
    }

    private void getExpectedValue(List<Map<String, Object>> rules, String name, String value) {
        Map<String, Object> rule = new HashMap<>();
        rule.put("name", name);
        rule.put("value", value);
        rules.add(rule);
    }

    private void getExpectedValueWithReconfigure(List<Map<String, Object>> rules, String name, String value,
                                                 String reconfigure) {
        Map<String, Object> rule = new HashMap<>();
        rule.put("name", name);
        rule.put("value", value);
        rule.put("canReconfigure", Boolean.valueOf(reconfigure));
        rules.add(rule);
    }

    @ParameterizedTest
    @CsvSource({
        "processApplication,"
            + "[Decide an application](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/decideAnApplication),",
        "adaProcessApplicationToUpdateHearingRequirements,"
            + "[Decide an application](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/decideAnApplication),",
        "adaProcessApplicationToUpdateAppealDetails,"
            + "[Decide an application](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/decideAnApplication),",
        "adaProcessApplicationToReinstateAnEndedAppeal,"
            + "[Decide an application](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/decideAnApplication),",
        "adaProcessApplicationToOther,"
            + "[Decide an application](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/decideAnApplication),",
        "adaLinkUnlinkAppeals,"
            + "[Decide an application](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/decideAnApplication),",
        "reviewTheAppeal,[Request respondent evidence]"
            + "(/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/requestRespondentEvidence),",
        "adaReviewTheAppeal,[Request respondent evidence]"
            + "(/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/requestRespondentEvidence),",
        "decideOnTimeExtension,"
            + "[Change the direction due date](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/changeDirectionDueDate),",
        "reviewRespondentEvidence,"
            + "[Request reasons for appeal](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/requestReasonsForAppeal)<br />"
            + "[Send non-standard direction](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/sendDirection),"
            + "aip",
        "reviewRespondentEvidence,"
            + "[Request case building](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/requestCaseBuilding)<br />"
            + "[Send non-standard direction](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/sendDirection),",
        "adaReviewRespondentEvidence,"
            + "[Request case building](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/requestCaseBuilding)<br />"
            + "[Send non-standard direction](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/sendDirection),",
        "reviewAppealSkeletonArgument,"
            + "[Request respondent review](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/requestRespondentReview)<br />"
            + "[Request case edit](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/requestCaseEdit),",
        "adaReviewAppealSkeletonArgument,"
            + "[Request respondent review](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/requestRespondentReview)<br />"
            + "[Request case edit](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/requestCaseEdit),",
        "reviewReasonsForAppeal,"
            + "[Request respondent review](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/requestRespondentReview)<br />"
            + "[Send direction with questions](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/sendDirectionWithQuestions),"
            + "aip",
        "reviewReasonsForAppeal,"
            + "[Request respondent review](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/requestRespondentReview)<br />"
            + "[Request CMA requirements](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/requestCmaRequirements),",
        "reviewCmaRequirements,"
            + "[Review CMA Requirements](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/reviewCmaRequirements),",
        "attendCma,"
            + "[Update case details after CMA](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/updateDetailsAfterCma),",
        "reviewRespondentResponse,"
            + "[Review Home Office response](/case/IA/Asylum/${[CASE_REFERENCE]}/"
            + "trigger/requestResponseReview)<br />[Amend appeal response](/case/IA/Asylum/${[CASE_REFERENCE]}/"
            + "trigger/requestResponseAmend),",
        "createHearingBundle,"
            + "[Generate the hearing bundle](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger"
            + "/generateHearingBundle)<br />"
            + "[Customise the hearing bundle](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/customiseHearingBundle),",
        "startDecisionsAndReasonsDocument,"
            + "[Start decision and reasons document](/case/IA/Asylum/${[CASE_REFERENCE]}"
            + "/trigger/decisionAndReasonsStarted/decisionAndReasonsStartedcaseIntroduction),",
        "reviewHearingRequirements,"
            + "[Review hearing requirements](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger"
            + "/reviewHearingRequirements),",
        "reviewAdditionalEvidence,[Review evidence](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/markEvidence"
            + "AsReviewed),",
        "adaReviewAdditionalEvidence,[Review evidence](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/markEvidence"
            + "AsReviewed),",
        "reviewAdditionalHomeOfficeEvidence,[Review evidence](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/markEvidence"
            + "AsReviewed),",
        "adaReviewAdditionalHomeOfficeEvidence,[Review evidence](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/"
            + "markEvidenceAsReviewed),",
        "arrangeOfflinePayment,[Mark the appeal as paid](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/markAppealPaid),",
        "markCaseAsPaid,[Mark the appeal as paid](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/markAppealPaid),",
        "allocateHearingJudge," + "[Allocate Hearing Judge](/role-access/allocate-role/allocate?caseId="
            + "${[CASE_REFERENCE]}&roleCategory=JUDICIAL&jurisdiction=IA),",
        "adaAllocateHearingJudge," + "[Allocate Hearing Judge](/role-access/allocate-role/allocate?caseId="
            + "${[CASE_REFERENCE]}&roleCategory=JUDICIAL&jurisdiction=IA),",
        "uploadHearingRecording,[Upload the hearing recording](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/upload"
            + "HearingRecording),",
        "generateDraftDecisionAndReasons,[Generate the draft decisions and reasons document](/case/IA/Asylum"
            + "/${[CASE_REFERENCE]}/trigger/generateDecisionAndReasons),",
        "reviewAddendumEvidence,[Review evidence](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/"
            + "markAddendumEvidenceAsReviewed),",
        "adaReviewAddendumEvidence,[Review evidence](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/"
            + "markAddendumEvidenceAsReviewed),",
        "editListing,[Edit case listing](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/editCaseListing),",
        "adaEditListing,[Edit case listing](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/editCaseListing),",
        "decideAnFTPA,[Leadership judge FTPA decision](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/"
            + "leadershipJudgeFtpaDecision)<br />"
            + "[Resident judge FTPA decision](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/residentJudgeFtpaDecision),",
        "adaDecideAnFTPA,[Leadership judge FTPA decision](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/"
            + "leadershipJudgeFtpaDecision)<br />"
            + "[Resident judge FTPA decision](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/residentJudgeFtpaDecision),",
        "prepareDecisionsAndReasons,[Prepare decisions and reasons](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/"
            + "generateDecisionAndReasons),",
        "adaPrepareDecisionsAndReasons,[Prepare decisions and reasons](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/"
            + "generateDecisionAndReasons),",
        "sendDecisionsAndReasons,[Complete decision and reasons](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/"
            + "sendDecisionAndReasons),",
        "adaSendDecisionsAndReasons,[Complete decision and reasons](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/"
            + "sendDecisionAndReasons),",
        "processApplicationToReviewDecision,[Decide an application](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/"
            + "decideAnApplication),",
        "adaProcessApplicationToAdjourn,[Decide an application](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/"
            + "decideAnApplication),",
        "adaProcessApplicationToExpedite,[Decide an application](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/"
            + "decideAnApplication),",
        "adaProcessApplicationForTimeExtension,[Decide an application](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/"
            + "decideAnApplication),",
        "adaProcessApplicationToWithdraw,[Decide an application](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/"
            + "decideAnApplication),",
        "adaProcessApplicationToReviewDecision,[Decide an application](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/"
            + "decideAnApplication),",
        "reviewCaseMarkedUnsuitableForADA,[Transfer the appeal out of the ADA process]"
            + "(/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/transferOutOfAda),",
        "reviewADASuitability,[Review ADA suitability](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/"
            + "adaSuitabilityReview),",
        "reviewSpecificAccessRequestJudiciary,[Review Access Request](/role-access/"
            + "${[taskId]}/assignment/${[roleAssignmentId]}/specific-access),",
        "reviewSpecificAccessRequestLegalOps,[Review Access Request](/role-access/"
            + "${[taskId]}/assignment/${[roleAssignmentId]}/specific-access),",
        "reviewSpecificAccessRequestAdmin,[Review Access Request](/role-access/"
            + "${[taskId]}/assignment/${[roleAssignmentId]}/specific-access),",
        "reviewSpecificAccessRequestCTSC,[Review Access Request](/role-access/"
            + "${[taskId]}/assignment/${[roleAssignmentId]}/specific-access),"
    })
    void should_return_a_200_description_property(String taskType, String expectedDescription, String journeyType) {
        VariableMap inputVariables = new VariableMapImpl();

        String roleAssignmentId = UUID.randomUUID().toString();
        String taskId = UUID.randomUUID().toString();
        Map<String, String> taskAttributes = Map.of("taskType", taskType,
                                                    "roleAssignmentId", roleAssignmentId,
                                                    "taskId", taskId);
        inputVariables.putValue("taskAttributes", taskAttributes);
        if (journeyType != null) {
            inputVariables.putValue("caseData", Map.of("journeyType", journeyType));
        }

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        List<Map<String, Object>> descriptionList = dmnDecisionTableResult.getResultList().stream()
            .filter((r) -> r.containsValue("description"))
            .collect(Collectors.toList());

        assertEquals(1, descriptionList.size());

        assertEquals(Map.of(
            "name", "description",
            "value", expectedDescription
                .replace("${[roleAssignmentId]}", roleAssignmentId).replace("${[taskId]}", taskId)
        ), descriptionList.get(0));

    }

}

