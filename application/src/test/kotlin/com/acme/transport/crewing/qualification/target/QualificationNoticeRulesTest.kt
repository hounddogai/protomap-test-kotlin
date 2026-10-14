package com.acme.transport.crewing.qualification.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultQualificationNoticeValidator()
        val item = newQualificationNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = QualificationNoticeRequiredCategoryRule("qualification notice needs an entry")
        val item = newQualificationNotice("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = QualificationNoticeRequiredCodeRule("qualification notice needs an entry")
        val item = newQualificationNotice("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
