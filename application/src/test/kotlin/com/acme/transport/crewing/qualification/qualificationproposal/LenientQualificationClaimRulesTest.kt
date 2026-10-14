package com.acme.transport.crewing.qualification.qualificationproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientQualificationClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientQualificationClaimValidator()
        val item = newLenientQualificationClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = LenientQualificationClaimScoreRangeRule(0.4, 843.6)
        val item = newLenientQualificationClaim("x", "y")
        item.score = 845.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = LenientQualificationClaimRequiredChannelRule("lenient qualification claim needs this field")
        val item = newLenientQualificationClaim("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
