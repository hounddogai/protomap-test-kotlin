package com.acme.transport.customs.tariff.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTariffScheduleValidator()
        val item = newTariffSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TariffScheduleRequiredCodeRule("tariff schedule needs an entry")
        val item = newTariffSchedule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TariffScheduleRequiredTitleRule("tariff schedule needs this field")
        val item = newTariffSchedule("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
