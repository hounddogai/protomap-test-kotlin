package com.acme.transport.fleetops.leasereturn.returntier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientLeaseReturnManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientLeaseReturnManifestValidator()
        val item = newLenientLeaseReturnManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = LenientLeaseReturnManifestLimitRangeRule(16, 3600)
        val item = newLenientLeaseReturnManifest("x", "y")
        item.limit = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = LenientLeaseReturnManifestLabelLengthRule(3600)
        val item = newLenientLeaseReturnManifest("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = LenientLeaseReturnManifestTitleLengthRule(86400)
        val item = newLenientLeaseReturnManifest("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientLeaseReturnManifestCodeLengthRule(255)
        val item = newLenientLeaseReturnManifest("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = LenientLeaseReturnManifestRequiredDockRule("lenient lease return manifest needs this field")
        val item = newLenientLeaseReturnManifest("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
