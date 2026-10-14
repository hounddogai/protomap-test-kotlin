package com.acme.transport.customs.clearance.clearancerequest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ClearanceDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultClearanceDigestEngine(newRegistry())
        val item = newClearanceDigest("code-329", "title-667")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
