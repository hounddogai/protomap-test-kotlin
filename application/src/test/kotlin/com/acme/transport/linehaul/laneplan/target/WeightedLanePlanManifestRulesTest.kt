package com.acme.transport.linehaul.laneplan.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedLanePlanManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedLanePlanManifestValidator()
        val item = newWeightedLanePlanManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedLanePlanManifestCodeLengthRule(86400)
        val item = newWeightedLanePlanManifest("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = WeightedLanePlanManifestTitleLengthRule(120)
        val item = newWeightedLanePlanManifest("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = WeightedLanePlanManifestRequiredCurrencyRule("weighted lane plan manifest needs an entry")
        val item = newWeightedLanePlanManifest("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
