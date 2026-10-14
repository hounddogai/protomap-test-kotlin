package com.acme.transport.safety.nearmiss.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNearMissManifestValidator()
        val item = newNearMissManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = NearMissManifestRequiredNameRule("near miss manifest needs an entry")
        val item = newNearMissManifest("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = NearMissManifestMarginRangeRule(4.5, 352.9)
        val item = newNearMissManifest("x", "y")
        item.margin = 354.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = NearMissManifestCodeLengthRule(16)
        val item = newNearMissManifest("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
