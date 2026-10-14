package com.acme.transport.terminal.crossdock.dockestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingCrossDockScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingCrossDockScoreStore()
        val item = newRollingCrossDockScore("code-127", "title-275")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
