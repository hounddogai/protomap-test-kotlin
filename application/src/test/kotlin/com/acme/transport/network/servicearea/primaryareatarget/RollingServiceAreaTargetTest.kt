package com.acme.transport.network.servicearea.primaryareatarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingServiceAreaTargetTest {
    @Test
    fun `starts in created`() {
        val item = newRollingServiceAreaTarget("code-948", "name-871")
        assertEquals(RollingServiceAreaTargetStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newRollingServiceAreaTarget("code-948", "name-871")
        try {
            item.moveTo(RollingServiceAreaTargetStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingServiceAreaTargetStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRollingServiceAreaTarget("code-948", "name-871")
        item.status = RollingServiceAreaTargetStatus.RETIRED
        assertFails("move") { item.moveTo(RollingServiceAreaTargetStatus.CREATED) }
    }
}
