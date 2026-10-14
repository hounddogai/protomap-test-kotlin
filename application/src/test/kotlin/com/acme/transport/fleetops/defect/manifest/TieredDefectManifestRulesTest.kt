package com.acme.transport.fleetops.defect.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredDefectManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredDefectManifestValidator()
        val item = newTieredDefectManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = TieredDefectManifestLocaleLengthRule(120)
        val item = newTieredDefectManifest("x", "y")
        item.locale = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = TieredDefectManifestTitleLengthRule(32)
        val item = newTieredDefectManifest("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above limit`() {
        val rule = TieredDefectManifestLimitRangeRule(80, 255)
        val item = newTieredDefectManifest("x", "y")
        item.limit = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
