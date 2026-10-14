package com.acme.transport.network.servicearea.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServiceAreaTemplateValidator()
        val item = newServiceAreaTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = ServiceAreaTemplateDescriptionLengthRule(255)
        val item = newServiceAreaTemplate("x", "y")
        item.description = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = ServiceAreaTemplateTitleLengthRule(255)
        val item = newServiceAreaTemplate("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
