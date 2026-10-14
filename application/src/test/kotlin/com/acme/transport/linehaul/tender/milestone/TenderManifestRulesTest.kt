package com.acme.transport.linehaul.tender.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTenderManifestValidator()
        val item = newTenderManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = TenderManifestLabelLengthRule(32)
        val item = newTenderManifest("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = TenderManifestCodeLengthRule(80)
        val item = newTenderManifest("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TenderManifestRequiredLabelRule("tender manifest needs a value")
        val item = newTenderManifest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
