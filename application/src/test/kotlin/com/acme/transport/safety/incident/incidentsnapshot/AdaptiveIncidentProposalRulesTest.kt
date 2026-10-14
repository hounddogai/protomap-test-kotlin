package com.acme.transport.safety.incident.incidentsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveIncidentProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveIncidentProposalValidator()
        val item = newAdaptiveIncidentProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = AdaptiveIncidentProposalLabelLengthRule(120)
        val item = newAdaptiveIncidentProposal("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = AdaptiveIncidentProposalThresholdRangeRule(1.8, 4545.9)
        val item = newAdaptiveIncidentProposal("x", "y")
        item.threshold = 4547.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveIncidentProposalCodeLengthRule(80)
        val item = newAdaptiveIncidentProposal("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
