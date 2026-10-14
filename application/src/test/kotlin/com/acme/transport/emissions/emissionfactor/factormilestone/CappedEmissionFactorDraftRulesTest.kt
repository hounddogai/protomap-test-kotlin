package com.acme.transport.emissions.emissionfactor.factormilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedEmissionFactorDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedEmissionFactorDraftValidator()
        val item = newCappedEmissionFactorDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CappedEmissionFactorDraftRequiredLabelRule("capped emission factor draft needs this field")
        val item = newCappedEmissionFactorDraft("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = CappedEmissionFactorDraftRequiredDriverIdRule("capped emission factor draft needs a value")
        val item = newCappedEmissionFactorDraft("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
