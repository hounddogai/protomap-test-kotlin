package com.acme.transport.customs.broker.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultBrokerDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultBrokerDigestStore()
        val item = newDefaultBrokerDigest("code-360", "title-476")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
