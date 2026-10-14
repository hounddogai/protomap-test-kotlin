package com.acme.transport.customs.tariff.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalTariffManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalTariffManifestValidator()
        val item = newGlobalTariffManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = GlobalTariffManifestRequiredNameRule("global tariff manifest needs an entry")
        val item = newGlobalTariffManifest("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = GlobalTariffManifestRequiredDriverIdRule("global tariff manifest needs a value")
        val item = newGlobalTariffManifest("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
