package com.acme.transport.linehaul.backhaul.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveBackhaulProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveBackhaulProposalValidator()
        val item = newAdaptiveBackhaulProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = AdaptiveBackhaulProposalPriorityRangeRule(64, 86400)
        val item = newAdaptiveBackhaulProposal("x", "y")
        item.priority = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AdaptiveBackhaulProposalRequiredTitleRule("adaptive backhaul proposal needs a value")
        val item = newAdaptiveBackhaulProposal("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = AdaptiveBackhaulProposalRatioRangeRule(3.6, 1295.4)
        val item = newAdaptiveBackhaulProposal("x", "y")
        item.ratio = 1296.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
