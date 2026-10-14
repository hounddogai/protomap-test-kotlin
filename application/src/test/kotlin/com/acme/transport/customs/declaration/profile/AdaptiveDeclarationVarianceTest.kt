package com.acme.transport.customs.declaration.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveDeclarationVarianceTest {
    @Test
    fun `starts in idle`() {
        val item = newAdaptiveDeclarationVariance("code-857", "name-37")
        assertEquals(AdaptiveDeclarationVarianceStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newAdaptiveDeclarationVariance("code-857", "name-37")
        try {
            item.moveTo(AdaptiveDeclarationVarianceStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveDeclarationVarianceStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAdaptiveDeclarationVariance("code-857", "name-37")
        item.status = AdaptiveDeclarationVarianceStatus.STOPPED
        assertFails("move") { item.moveTo(AdaptiveDeclarationVarianceStatus.IDLE) }
    }
}
