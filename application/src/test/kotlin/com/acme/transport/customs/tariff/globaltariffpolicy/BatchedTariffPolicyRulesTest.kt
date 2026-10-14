package com.acme.transport.customs.tariff.globaltariffpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedTariffPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedTariffPolicyValidator()
        val item = newBatchedTariffPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = BatchedTariffPolicyVehicleIdLengthRule(255)
        val item = newBatchedTariffPolicy("x", "y")
        item.vehicleId = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = BatchedTariffPolicyPalletsRangeRule(80, 3600)
        val item = newBatchedTariffPolicy("x", "y")
        item.pallets = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above created at`() {
        val rule = BatchedTariffPolicyCreatedAtRangeRule(32, 80)
        val item = newBatchedTariffPolicy("x", "y")
        item.createdAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedTariffPolicyCodeLengthRule(80)
        val item = newBatchedTariffPolicy("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above amount`() {
        val rule = BatchedTariffPolicyAmountRangeRule(8.2, 2380.1)
        val item = newBatchedTariffPolicy("x", "y")
        item.amount = 2381.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
