package com.acme.transport.customs.broker.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingBrokerScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingBrokerScoreStore()
        val item = newRollingBrokerScore("code-659", "title-671")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
