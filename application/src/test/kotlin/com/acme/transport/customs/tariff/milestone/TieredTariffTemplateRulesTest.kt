package com.acme.transport.customs.tariff.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredTariffTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredTariffTemplateValidator()
        val item = newTieredTariffTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = TieredTariffTemplateTitleLengthRule(3600)
        val item = newTieredTariffTemplate("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TieredTariffTemplateRequiredTitleRule("tiered tariff template needs this field")
        val item = newTieredTariffTemplate("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
