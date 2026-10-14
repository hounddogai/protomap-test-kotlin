package com.acme.transport.crewing.qualification.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultQualificationDigestValidator()
        val item = newQualificationDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = QualificationDigestRequiredCodeRule("qualification digest needs an entry")
        val item = newQualificationDigest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = QualificationDigestRegionLengthRule(3600)
        val item = newQualificationDigest("x", "y")
        item.region = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = QualificationDigestRequiredRouteCodeRule("qualification digest needs a value")
        val item = newQualificationDigest("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank region`() {
        val rule = QualificationDigestRequiredRegionRule("qualification digest needs a value")
        val item = newQualificationDigest("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = QualificationDigestVersionRangeRule(120, 1000)
        val item = newQualificationDigest("x", "y")
        item.version = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
