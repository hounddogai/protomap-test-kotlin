package com.acme.transport.network.servicearea.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaClaimTest {
    @Test
    fun `starts in created`() {
        val item = newServiceAreaClaim("code-135", "title-601")
        assertEquals(ServiceAreaClaimStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newServiceAreaClaim("code-135", "title-601")
        try {
            item.moveTo(ServiceAreaClaimStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaClaimStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newServiceAreaClaim("code-135", "title-601")
        item.status = ServiceAreaClaimStatus.RETIRED
        assertFails("move") { item.moveTo(ServiceAreaClaimStatus.CREATED) }
    }
}
