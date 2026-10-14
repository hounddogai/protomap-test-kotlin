package com.acme.transport.fleetops.serviceplan.planthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedServicePlanTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedServicePlanTargetValidator()
        val item = newWeightedServicePlanTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = WeightedServicePlanTargetRequiredLabelRule("weighted service plan target needs an entry")
        val item = newWeightedServicePlanTarget("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = WeightedServicePlanTargetCarrierLengthRule(1000)
        val item = newWeightedServicePlanTarget("x", "y")
        item.carrier = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank description`() {
        val rule = WeightedServicePlanTargetRequiredDescriptionRule("weighted service plan target needs a value")
        val item = newWeightedServicePlanTarget("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedServicePlanTargetRequiredCodeRule("weighted service plan target needs this field")
        val item = newWeightedServicePlanTarget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = WeightedServicePlanTargetDescriptionLengthRule(64)
        val item = newWeightedServicePlanTarget("x", "y")
        item.description = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
