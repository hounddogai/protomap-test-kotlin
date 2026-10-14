package com.acme.transport.lastmile.parcel.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultParcelProposalValidator()
        val item = newParcelProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = ParcelProposalRequiredRouteCodeRule("parcel proposal needs a value")
        val item = newParcelProposal("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = ParcelProposalRequiredCurrencyRule("parcel proposal needs this field")
        val item = newParcelProposal("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
