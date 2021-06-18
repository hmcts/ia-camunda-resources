package uk.gov.hmcts.reform.iataskconfiguration.dmn;

import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableImpl;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.iataskconfiguration.DmnDecisionTableBaseUnitTest;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.iataskconfiguration.DmnDecisionTable.WA_TASK_COMPLETION_IA_ASYLUM;

class CamundaTaskCompletionTest extends DmnDecisionTableBaseUnitTest {

    @BeforeAll
    public static void initialization() {
        CURRENT_DMN_DECISION_TABLE = WA_TASK_COMPLETION_IA_ASYLUM;
    }

    static Stream<Arguments> scenarioProvider() {

        return Stream.of(
            Arguments.of(
                "requestRespondentEvidence",
                singletonList(
                    Map.of(
                        "taskType", "reviewTheAppeal",
                        "completionMode", "Auto"
                    )
                )
            ),
            Arguments.of(
                "requestCaseBuilding",
                singletonList(
                    Map.of(
                        "taskType", "reviewRespondentEvidence",
                        "completionMode", "Auto"
                    )
                )
            ),
            Arguments.of(
                "requestReasonsForAppeal",
                singletonList(
                    Map.of(
                        "taskType", "reviewRespondentEvidence",
                        "completionMode", "Auto"
                    )
                )
            ),
            Arguments.of(
                "sendDirection",
                asList(
                    Map.of(
                        "taskType", "reviewRespondentEvidence",
                        "completionMode", "Auto"
                    ),
                    Map.of(
                        "taskType", "reviewAdditionalHomeOfficeEvidence",
                        "completionMode", "Auto"
                    ),
                    Map.of(
                        "taskType", "reviewAdditionalAppellantEvidence",
                        "completionMode", "Auto"
                    )
                )
            ),
            Arguments.of(
                "requestResponseReview",
                asList(
                    Map.of(
                        "taskType", "reviewAppealSkeletonArgument",
                        "completionMode", "Auto"
                    ),
                    Map.of(
                        "taskType", "reviewReasonsForAppeal",
                        "completionMode", "Auto"
                    ),
                    Map.of(
                        "taskType", "reviewClarifyingQuestionsAnswers",
                        "completionMode", "Auto"
                    ),
                    Map.of(
                        "taskType", "reviewRespondentResponse",
                        "completionMode", "Auto"
                    )
                )
            ),
            Arguments.of(
                "requestCaseEdit",
                singletonList(
                    Map.of(
                        "taskType", "reviewAppealSkeletonArgument",
                        "completionMode", "Auto"
                    )
                )
            ),
            Arguments.of(
                "sendDirectionWithQuestions",
                asList(
                    Map.of(
                        "taskType", "reviewReasonsForAppeal",
                        "completionMode", "Auto"
                    ),
                    Map.of(
                        "taskType", "reviewClarifyingQuestionsAnswers",
                        "completionMode", "Auto"
                    )
                )
            ),
            Arguments.of(
                "requestCmaRequirements",
                asList(
                    Map.of(
                        "taskType", "reviewReasonsForAppeal",
                        "completionMode", "Auto"
                    ),
                    Map.of(
                        "taskType", "reviewClarifyingQuestionsAnswers",
                        "completionMode", "Auto"
                    )
                )
            ),
            Arguments.of(
                "reviewCmaRequirements",
                singletonList(
                    Map.of(
                        "taskType", "reviewCmaRequirements",
                        "completionMode", "Auto"
                    )
                )
            ),
            Arguments.of(
                "updateDetailsAfterCma",
                singletonList(
                    Map.of(
                        "taskType", "attendCma",
                        "completionMode", "Auto"
                    )
                )
            ),
            Arguments.of(
                "requestResponseAmend",
                singletonList(
                    Map.of(
                        "taskType", "reviewRespondentResponse",
                        "completionMode", "Auto"
                    )
                )
            ),
            Arguments.of(
                "reviewHearingRequirements",
                singletonList(
                    Map.of(
                        "taskType", "reviewHearingRequirements",
                        "completionMode", "Auto"
                    )
                )
            ),
            Arguments.of(
                "createCaseSummary",
                singletonList(
                    Map.of(
                        "taskType", "createCaseSummary",
                        "completionMode", "Auto"
                    )
                )
            ),
            Arguments.of(
                "generateHearingBundle",
                singletonList(
                    Map.of(
                        "taskType", "createHearingBundle",
                        "completionMode", "Auto"
                    )
                )
            ),
            Arguments.of(
                "decisionAndReasonsStarted",
                singletonList(
                    Map.of(
                        "taskType", "startDecisionsAndReasonsDocument",
                        "completionMode", "Auto"
                    )
                )
            ),
            Arguments.of(
                "recordAllocatedJudge",
                singletonList(
                    Map.of(
                        "taskType", "allocateFtpaToJudge",
                        "completionMode", "Auto"
                    )
                )
            ),
            Arguments.of(
                "changeDirectionDueDate",
                singletonList(
                    Map.of(
                        "taskType", "decideOnTimeExtension",
                        "completionMode", "Auto"
                    )
                )
            ),
            Arguments.of(
                "decideAnApplication",
                singletonList(
                    Map.of(
                        "taskType", "processApplication",
                        "completionMode", "Auto"
                    )
                )
            ),
            Arguments.of(
                "unknownEvent",
                emptyList()
            )
        );
    }

    @ParameterizedTest(name = "event id: {0}")
    @MethodSource("scenarioProvider")
    void given_event_ids_should_evaluate_dmn(String eventId, List<Map<String, String>> expectation) {

        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", eventId);

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        MatcherAssert.assertThat(dmnDecisionTableResult.getResultList(), is(expectation));
    }

    @Test
    void if_this_test_fails_needs_updating_with_your_changes() {

        //The purpose of this test is to prevent adding new rows without being tested
        DmnDecisionTableImpl logic = (DmnDecisionTableImpl) decision.getDecisionLogic();
        assertThat(logic.getRules().size(), is(17));

    }


}
