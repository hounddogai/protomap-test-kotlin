package com.acme.transport.emissions.routeoffset.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetAgreementTest {
    @Test
    fun `starts in created`() {
        val item = newRouteOffsetAgreement("code-45", "title-683")
        assertEquals(RouteOffsetAgreementStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRouteOffsetAgreement("code-45", "title-683")
        try {
            item.moveTo(RouteOffsetAgreementStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetAgreementStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRouteOffsetAgreement("code-45", "title-683")
        item.status = RouteOffsetAgreementStatus.RETIRED
        assertFails("move") { item.moveTo(RouteOffsetAgreementStatus.CREATED) }
    }
}
