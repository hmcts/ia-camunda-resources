package uk.gov.hmcts.reform.iataskconfiguration.dmn;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableImpl;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.iataskconfiguration.DmnDecisionTableBaseUnitTest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.iataskconfiguration.DmnDecisionTable.WA_TASK_INITIATION_IA_ASYLUM;

class CamundaTaskInitiationTest extends DmnDecisionTableBaseUnitTest {

    @BeforeAll
    public static void initialization() {
        CURRENT_DMN_DECISION_TABLE = WA_TASK_INITIATION_IA_ASYLUM;
    }

    static Stream<Arguments> scenarioProvider() {

        return Stream.of(
            Arguments.of(
                "makeAnApplication",
                null,
                null,
                singletonList(
                    Map.of(
                        "taskId", "processApplication",
                        "name", "Process Application",
                        "group", "TCW",
                        "workingDaysAllowed", 2
                    )
                )
            ),
            Arguments.of(
                "submitAppeal",
                "appealSubmitted",
                mapAppealType("refusalOfHumanRights"),
                asList(
                    Map.of(
                        "taskId", "checkFeeStatus",
                        "name", "Check Fee Status",
                        "group", "TCW",
                        "workingDaysAllowed", 2,
                        "delayDuration", 28,
                        "processCategories",  "followUpOverdue"
                    ),
                    Map.of(
                        "taskId", "reviewTheAppeal",
                        "name", "Review the appeal",
                        "group", "TCW",
                        "workingDaysAllowed", 2,
                        "processCategories",  "caseProgression"
                    )
                )
            ),
            Arguments.of(
                "submitAppeal",
                "appealSubmitted",
                mapAppealType("refusalOfEu"),
                asList(
                    Map.of(
                        "taskId", "checkFeeStatus",
                        "name", "Check Fee Status",
                        "group", "TCW",
                        "workingDaysAllowed", 2,
                        "delayDuration", 28,
                        "processCategories",  "followUpOverdue"
                    ),
                    Map.of(
                        "taskId", "reviewTheAppeal",
                        "name", "Review the appeal",
                        "group", "TCW",
                        "workingDaysAllowed", 2,
                        "processCategories",  "caseProgression"
                    )
                )
            ),
            Arguments.of(
                "submitAppeal",
                "appealSubmitted",
                mapAppealType("protection"),
                asList(
                    Map.of(
                        "taskId", "checkFeeStatus",
                        "name", "Check Fee Status",
                        "group", "TCW",
                        "workingDaysAllowed", 2,
                        "delayDuration", 28,
                        "processCategories",  "followUpOverdue"
                    ),
                    Map.of(
                        "taskId", "reviewTheAppeal",
                        "name", "Review the appeal",
                        "group", "TCW",
                        "workingDaysAllowed", 2,
                        "processCategories",  "caseProgression"
                    )
                )
            ),
            Arguments.of(
                "submitAppeal",
                "appealSubmitted",
                null,
                singletonList(
                    Map.of(
                        "taskId", "reviewTheAppeal",
                        "name", "Review the appeal",
                        "group", "TCW",
                        "workingDaysAllowed", 2,
                        "processCategories",  "caseProgression"
                    )
                )
            ),
            Arguments.of(
                "submitTimeExtension",
                "null",
                null,
                singletonList(
                    Map.of(
                        "taskId", "decideOnTimeExtension",
                        "name", "Decide On Time Extension",
                        "group", "TCW",
                        "workingDaysAllowed", 2,
                        "processCategories", "timeExtension"
                    )
                )
            ),
            Arguments.of(
                "uploadHomeOfficeBundle",
                "awaitingRespondentEvidence",
                null,
                singletonList(
                    Map.of(
                        "taskId", "reviewRespondentEvidence",
                        "name", "Review Respondent Evidence",
                        "group", "TCW",
                        "workingDaysAllowed", 2,
                        "processCategories",  "caseProgression"
                    )
                )
            ),
            Arguments.of(
                "submitCase",
                "caseUnderReview",
                null,
                singletonList(
                    Map.of(
                        "taskId", "reviewAppealSkeletonArgument",
                        "name", "Review Appeal Skeleton Argument",
                        "group", "TCW",
                        "workingDaysAllowed", 2,
                        "processCategories",  "caseProgression"
                    )
                )
            ),
            Arguments.of(
                "buildCase",
                "caseUnderReview",
                null,
                singletonList(
                    Map.of(
                        "taskId", "reviewAppealSkeletonArgument",
                        "name", "Review Appeal Skeleton Argument",
                        "group", "TCW",
                        "workingDaysAllowed", 2,
                        "processCategories",  "caseProgression"
                    )
                )
            ),
            Arguments.of(
                "submitReasonsForAppeal",
                "reasonsForAppealSubmitted",
                null,
                singletonList(
                    Map.of(
                        "taskId", "reviewReasonsForAppeal",
                        "name", "Review Reasons For Appeal",
                        "group", "TCW",
                        "workingDaysAllowed", 2,
                        "processCategories",  "caseProgression"
                    )
                )
            ),
            Arguments.of(
                "submitClarifyingQuestionAnswers",
                "clarifyingQuestionsAnswersSubmitted",
                null,
                singletonList(
                    Map.of(
                        "taskId", "reviewClarifyingQuestionsAnswers",
                        "name", "Review Clarifying Questions Answers",
                        "group", "TCW",
                        "workingDaysAllowed", 2,
                        "processCategories",  "caseProgression"
                    )
                )
            ),
            Arguments.of(
                "submitCmaRequirements",
                "cmaRequirementsSubmitted",
                null,
                singletonList(
                    Map.of(
                        "taskId", "reviewCmaRequirements",
                        "name", "Review Cma Requirements",
                        "group", "TCW",
                        "workingDaysAllowed", 2,
                        "processCategories",  "caseProgression"
                    )
                )
            ),
            Arguments.of(
                "listCma",
                "cmaListed",
                null,
                singletonList(
                    Map.of(
                        "taskId", "attendCma",
                        "name", "Attend Cma",
                        "group", "TCW",
                        "workingDaysAllowed", 2,
                        "processCategories",  "caseProgression"
                    )
                )
            ),
            Arguments.of(
                "uploadHomeOfficeAppealResponse",
                "respondentReview",
                null,
                singletonList(
                    Map.of(
                        "taskId", "reviewRespondentResponse",
                        "name", "Review Respondent Response",
                        "group", "TCW",
                        "workingDaysAllowed", 2,
                        "processCategories",  "caseProgression"
                    )
                )
            ),
            Arguments.of(
                null,
                "prepareForHearing",
                null,
                singletonList(
                    Map.of(
                        "taskId", "createCaseSummary",
                        "name", "Create Case Summary",
                        "group", "TCW",
                        "workingDaysAllowed", 2,
                        "processCategories",  "caseProgression"
                    )
                )
            ),
            Arguments.of(
                null,
                "finalBundling",
                null,
                singletonList(
                    Map.of(
                        "taskId", "createHearingBundle",
                        "name", "Create Hearing Bundle",
                        "group", "TCW",
                        "workingDaysAllowed", 2,
                        "processCategories",  "caseProgression"
                    )
                )
            ),
            Arguments.of(
                null,
                "preHearing",
                null,
                singletonList(
                    Map.of(
                        "taskId", "startDecisionsAndReasonsDocument",
                        "name", "Start Decisions And Reasons Document",
                        "group", "TCW",
                        "workingDaysAllowed", 2,
                        "processCategories",  "caseProgression"
                    )
                )
            ),
            Arguments.of(
                "draftHearingRequirements",
                "listing",
                null,
                singletonList(
                    Map.of(
                        "taskId", "reviewHearingRequirements",
                        "name", "Review hearing requirements",
                        "group", "TCW",
                        "workingDaysAllowed", 2,
                        "processCategories",  "caseProgression"
                    )
                )
            ),
            Arguments.of(
                "requestRespondentEvidence",
                "awaitingRespondentEvidence",
                null,
                singletonList(
                    Map.of(
                        "taskId", "followUpOverdueRespondentEvidence",
                        "name", "Follow-up overdue respondent evidence",
                        "group", "TCW",
                        "workingDaysAllowed", 2,
                        "delayDuration", 0,
                        "processCategories",  "followUpOverdue"
                    )
                )
            ),
            Arguments.of(
                "requestCaseBuilding",
                "caseBuilding",
                null,
                singletonList(
                    Map.of(
                        "taskId", "followUpOverdueCaseBuilding",
                        "name", "Follow-up overdue case building",
                        "group", "TCW",
                        "workingDaysAllowed", 2,
                        "delayDuration", 0,
                        "processCategories",  "followUpOverdue"
                    )
                )
            ),
            Arguments.of(
                "requestReasonsForAppeal",
                "awaitingReasonsForAppeal",
                null,
                singletonList(
                    Map.of(
                        "taskId", "followUpOverdueReasonsForAppeal",
                        "name", "Follow-up overdue reasons for appeal",
                        "group", "TCW",
                        "workingDaysAllowed", 2,
                        "delayDuration", 0,
                        "processCategories",  "followUpOverdue"
                    )
                )
            ),
            Arguments.of(
                "sendDirectionWithQuestions",
                "awaitingClarifyingQuestionsAnswers",
                null,
                singletonList(
                    Map.of(
                        "taskId", "followUpOverdueClarifyingAnswers",
                        "name", "Follow-up overdue clarifying answers",
                        "group", "TCW",
                        "workingDaysAllowed", 2,
                        "delayDuration", 0,
                        "processCategories",  "followUpOverdue"
                    )
                )
            ),
            Arguments.of(
                "requestCmaRequirements",
                "awaitingCmaRequirements",
                null,
                singletonList(
                    Map.of(
                        "taskId", "followUpOverdueCmaRequirements",
                        "name", "Follow-up overdue CMA requirements",
                        "group", "TCW",
                        "workingDaysAllowed", 2,
                        "delayDuration", 0,
                        "processCategories",  "followUpOverdue"
                    )
                )
            ),
            Arguments.of(
                "requestRespondentReview",
                "respondentReview",
                null,
                singletonList(
                    Map.of(
                        "taskId", "followUpOverdueRespondentReview",
                        "name", "Follow-up overdue respondent review",
                        "group", "TCW",
                        "workingDaysAllowed", 2,
                        "delayDuration", 0,
                        "processCategories",  "followUpOverdue"
                    )
                )
            ),
            Arguments.of(
                "requestHearingRequirementsFeature",
                "submitHearingRequirements",
                null,
                singletonList(
                    Map.of(
                        "taskId", "followUpOverdueHearingRequirements",
                        "name", "Follow-up overdue hearing requirements",
                        "group", "TCW",
                        "workingDaysAllowed", 2,
                        "delayDuration", 0,
                        "processCategories",  "followUpOverdue"
                    )
                )
            ),
            Arguments.of(
                "sendDirection",
                null,
                null,
                singletonList(
                    Map.of(
                        "taskId", "followUpNonStandardDirection",
                        "name", "Follow-up non-standard direction",
                        "group", "TCW",
                        "workingDaysAllowed", 2,
                        "delayDuration", 0,
                        "processCategories",  "followUpOverdue"
                    )
                )
            ),
            Arguments.of(
                "removeRepresentation",
                null,
                null,
                singletonList(
                    Map.of(
                        "taskId", "followUpNoticeOfChange",
                        "name", "Follow-up Notice of Change",
                        "group", "TCW",
                        "workingDaysAllowed", 2,
                        "processCategories",  "followUpOverdue",
                        "delayDuration", 14
                    )
                )
            ),
            Arguments.of(
                "removeLegalRepresentative",
                null,
                null,
                singletonList(
                    Map.of(
                        "taskId", "followUpNoticeOfChange",
                        "name", "Follow-up Notice of Change",
                        "group", "TCW",
                        "workingDaysAllowed", 2,
                        "processCategories",  "followUpOverdue",
                        "delayDuration", 14
                    )
                )
            ),
            Arguments.of(
                "unknownEvent",
                null,
                null,
                emptyList()
            )
        );
    }

    @ParameterizedTest(name = "event id: {0} post event state: {1} appeal type: {2}")
    @MethodSource("scenarioProvider")
    void given_multiple_event_ids_should_evaluate_dmn(String eventId,
                                                      String postEventState,
                                                      Map<String, Object> map,
                                                      List<Map<String, String>> expectation) {

        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", eventId);
        inputVariables.putValue("postEventState", postEventState);
        inputVariables.putValue("caseData", map);

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        assertThat(dmnDecisionTableResult.getResultList(), is(expectation));
    }

    @Test
    void if_this_test_fails_needs_updating_with_your_changes() {

        //The purpose of this test is to prevent adding new rows without being tested
        DmnDecisionTableImpl logic = (DmnDecisionTableImpl) decision.getDecisionLogic();
        assertThat(logic.getRules().size(), is(24));

    }

    private static Map<String, Object> mapAppealType(String appealType) {
        String appealTypeJson = "{\n"
               + "   \"Data\":{\n"
               + "      \"appealType\":\"" + appealType + "\"\n"
               + "   }"
               + "}";

        ObjectMapper mapper = new ObjectMapper();
        try {
            TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>() {};
            return mapper.readValue(appealTypeJson, typeRef);
        } catch (IOException exp) {
            return null;
        }
    }
}
