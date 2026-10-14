package com.acme.transport.customs.broker.lenientbrokerestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BrokerMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBrokerMilestoneStore()
        val item = newBrokerMilestone("code-590", "name-284")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
