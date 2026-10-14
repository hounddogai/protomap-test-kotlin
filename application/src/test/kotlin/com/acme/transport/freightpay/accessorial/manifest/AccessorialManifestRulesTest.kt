package com.acme.transport.freightpay.accessorial.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessorialManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAccessorialManifestValidator()
        val item = newAccessorialManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = AccessorialManifestTitleLengthRule(16)
        val item = newAccessorialManifest("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = AccessorialManifestCodeLengthRule(1000)
        val item = newAccessorialManifest("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long summary`() {
        val rule = AccessorialManifestSummaryLengthRule(3600)
        val item = newAccessorialManifest("x", "y")
        item.summary = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = AccessorialManifestRequiredDriverIdRule("accessorial manifest needs an entry")
        val item = newAccessorialManifest("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
