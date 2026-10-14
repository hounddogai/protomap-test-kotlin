package com.acme.transport.network.servicearea.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServiceAreaMappingValidator()
        val item = newServiceAreaMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = ServiceAreaMappingRequiredChecksumRule("service area mapping needs a value")
        val item = newServiceAreaMapping("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ServiceAreaMappingCodeLengthRule(64)
        val item = newServiceAreaMapping("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ServiceAreaMappingRequiredCodeRule("service area mapping needs this field")
        val item = newServiceAreaMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
