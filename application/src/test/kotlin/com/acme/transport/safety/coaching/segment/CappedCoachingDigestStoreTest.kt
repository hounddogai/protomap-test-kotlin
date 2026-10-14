package com.acme.transport.safety.coaching.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedCoachingDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedCoachingDigestStore()
        val item = newCappedCoachingDigest("code-444", "name-189")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
