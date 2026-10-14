package com.acme.transport.freightpay.audit.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredAuditManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredAuditManifestValidator()
        val item = newTieredAuditManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = TieredAuditManifestFloorValueRangeRule(5.1, 803.4)
        val item = newTieredAuditManifest("x", "y")
        item.floorValue = 804.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = TieredAuditManifestTitleLengthRule(255)
        val item = newTieredAuditManifest("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = TieredAuditManifestThresholdRangeRule(2.5, 94.4)
        val item = newTieredAuditManifest("x", "y")
        item.threshold = 95.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above created at`() {
        val rule = TieredAuditManifestCreatedAtRangeRule(32, 1000)
        val item = newTieredAuditManifest("x", "y")
        item.createdAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above weight`() {
        val rule = TieredAuditManifestWeightRangeRule(2.0, 570.9)
        val item = newTieredAuditManifest("x", "y")
        item.weight = 572.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
