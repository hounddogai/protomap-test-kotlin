package com.acme.transport.fleetops.serviceplan.planmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalServicePlanDigestTest {
    @Test
    fun `starts in requested`() {
        val item = newGlobalServicePlanDigest("code-102", "title-930")
        assertEquals(GlobalServicePlanDigestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newGlobalServicePlanDigest("code-102", "title-930")
        try {
            item.moveTo(GlobalServicePlanDigestStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalServicePlanDigestStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newGlobalServicePlanDigest("code-102", "title-930")
        item.status = GlobalServicePlanDigestStatus.EXPIRED
        assertFails("move") { item.moveTo(GlobalServicePlanDigestStatus.REQUESTED) }
    }
}
