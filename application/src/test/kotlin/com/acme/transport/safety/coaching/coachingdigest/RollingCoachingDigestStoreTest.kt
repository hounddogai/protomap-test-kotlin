package com.acme.transport.safety.coaching.coachingdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingCoachingDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingCoachingDigestStore()
        val item = newRollingCoachingDigest("code-286", "label-118")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
