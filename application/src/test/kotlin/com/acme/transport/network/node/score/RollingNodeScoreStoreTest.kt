package com.acme.transport.network.node.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingNodeScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingNodeScoreStore()
        val item = newRollingNodeScore("code-167", "title-796")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
