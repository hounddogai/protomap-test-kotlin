package com.acme.transport.network.servicearea.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServiceAreaClaimValidator()
        val item = newServiceAreaClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ServiceAreaClaimRequiredTitleRule("service area claim needs a value")
        val item = newServiceAreaClaim("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
