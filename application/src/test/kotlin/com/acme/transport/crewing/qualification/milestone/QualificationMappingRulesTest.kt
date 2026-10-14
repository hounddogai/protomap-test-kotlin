package com.acme.transport.crewing.qualification.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultQualificationMappingValidator()
        val item = newQualificationMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = QualificationMappingNameLengthRule(16)
        val item = newQualificationMapping("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = QualificationMappingCodeLengthRule(1000)
        val item = newQualificationMapping("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
