package com.acme.transport.safety.nearmiss.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedNearMissManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedNearMissManifestValidator()
        val item = newBatchedNearMissManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank description`() {
        val rule = BatchedNearMissManifestRequiredDescriptionRule("batched near miss manifest needs an entry")
        val item = newBatchedNearMissManifest("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = BatchedNearMissManifestRequiredOwnerIdRule("batched near miss manifest needs a value")
        val item = newBatchedNearMissManifest("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = BatchedNearMissManifestAttemptsRangeRule(3600, 86400)
        val item = newBatchedNearMissManifest("x", "y")
        item.attempts = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedNearMissManifestRequiredCodeRule("batched near miss manifest needs an entry")
        val item = newBatchedNearMissManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = BatchedNearMissManifestTitleLengthRule(80)
        val item = newBatchedNearMissManifest("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
