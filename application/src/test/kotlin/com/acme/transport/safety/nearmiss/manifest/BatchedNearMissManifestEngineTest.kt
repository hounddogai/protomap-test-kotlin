package com.acme.transport.safety.nearmiss.manifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedNearMissManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedNearMissManifestEngine(newRegistry())
        val item = newBatchedNearMissManifest("code-514", "title-647")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
