package com.acme.transport.safety.cameraevent.snapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingCameraEventSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingCameraEventSnapshotEngine(newRegistry())
        val item = newRollingCameraEventSnapshot("code-188", "title-641")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
