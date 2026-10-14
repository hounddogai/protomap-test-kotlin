package com.acme.transport.fleetops.serviceplan.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientServicePlanDigestTest {
    @Test
    fun `starts in created`() {
        val item = newLenientServicePlanDigest("code-195", "title-759")
        assertEquals(LenientServicePlanDigestStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newLenientServicePlanDigest("code-195", "title-759")
        try {
            item.moveTo(LenientServicePlanDigestStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientServicePlanDigestStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLenientServicePlanDigest("code-195", "title-759")
        item.status = LenientServicePlanDigestStatus.RETIRED
        assertFails("move") { item.moveTo(LenientServicePlanDigestStatus.CREATED) }
    }
}
