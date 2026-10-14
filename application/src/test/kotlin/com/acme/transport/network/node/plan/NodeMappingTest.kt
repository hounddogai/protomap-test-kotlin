package com.acme.transport.network.node.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NodeMappingTest {
    @Test
    fun `starts in created`() {
        val item = newNodeMapping("code-786", "name-865")
        assertEquals(NodeMappingStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newNodeMapping("code-786", "name-865")
        try {
            item.moveTo(NodeMappingStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NodeMappingStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newNodeMapping("code-786", "name-865")
        item.status = NodeMappingStatus.RETIRED
        assertFails("move") { item.moveTo(NodeMappingStatus.CREATED) }
    }
}
