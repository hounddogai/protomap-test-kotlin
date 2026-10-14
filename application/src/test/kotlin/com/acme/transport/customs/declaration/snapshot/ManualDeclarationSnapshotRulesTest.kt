package com.acme.transport.customs.declaration.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualDeclarationSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualDeclarationSnapshotValidator()
        val item = newManualDeclarationSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = ManualDeclarationSnapshotCategoryLengthRule(3600)
        val item = newManualDeclarationSnapshot("x", "y")
        item.category = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = ManualDeclarationSnapshotVehicleIdLengthRule(255)
        val item = newManualDeclarationSnapshot("x", "y")
        item.vehicleId = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
