package com.acme.transport.network.servicearea.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedServiceAreaProfileTest {
    @Test
    fun `starts in created`() {
        val item = newCappedServiceAreaProfile("code-787", "name-382")
        assertEquals(CappedServiceAreaProfileStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newCappedServiceAreaProfile("code-787", "name-382")
        try {
            item.moveTo(CappedServiceAreaProfileStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedServiceAreaProfileStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCappedServiceAreaProfile("code-787", "name-382")
        item.status = CappedServiceAreaProfileStatus.RETIRED
        assertFails("move") { item.moveTo(CappedServiceAreaProfileStatus.CREATED) }
    }
}
