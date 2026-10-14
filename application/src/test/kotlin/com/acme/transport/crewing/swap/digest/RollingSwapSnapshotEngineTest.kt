package com.acme.transport.crewing.swap.digest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingSwapSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingSwapSnapshotEngine(newRegistry())
        val item = newRollingSwapSnapshot("code-762", "title-501")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
