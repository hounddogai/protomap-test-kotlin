package com.acme.transport.safety.nearmiss.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NearMissMappingTest {
    @Test
    fun `starts in proposed`() {
        val item = newNearMissMapping("code-413", "title-501")
        assertEquals(NearMissMappingStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newNearMissMapping("code-413", "title-501")
        try {
            item.moveTo(NearMissMappingStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NearMissMappingStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newNearMissMapping("code-413", "title-501")
        item.status = NearMissMappingStatus.WITHDRAWN
        assertFails("move") { item.moveTo(NearMissMappingStatus.PROPOSED) }
    }
}
