package com.acme.transport.safety.cameraevent.eventbundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps pending camera event bundles by code. */
interface PendingCameraEventBundleStore {
    fun find(code: String): PendingCameraEventBundle?
    fun save(item: PendingCameraEventBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingCameraEventBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingCameraEventBundleStore(
    var items: MutableMap<String, PendingCameraEventBundle>,
    var order: MutableList<String>,
) : PendingCameraEventBundleStore {
    override fun find(code: String): PendingCameraEventBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingCameraEventBundle) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<PendingCameraEventBundle> {
        val out = mutableListOf<PendingCameraEventBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingCameraEventBundleStore(): PendingCameraEventBundleStore {
    return MemoryPendingCameraEventBundleStore(
        mutableMapOf<String, PendingCameraEventBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingCameraEventBundleStore(
    var inner: PendingCameraEventBundleStore,
    var cache: TtlCache<PendingCameraEventBundle>,
    var hits: Int,
    var misses: Int,
) : PendingCameraEventBundleStore {
    override fun find(code: String): PendingCameraEventBundle? {
        val hit = (cache.get(code) as PendingCameraEventBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingCameraEventBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingCameraEventBundle> = inner.list()
}

fun newCachedPendingCameraEventBundleStore(inner: PendingCameraEventBundleStore, ttl: Int, clock: Clock): PendingCameraEventBundleStore {
    return CachedPendingCameraEventBundleStore(
        inner = inner,
        cache = newTtlCache<PendingCameraEventBundle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending camera event bundles. Empty fields match all. */
data class PendingCameraEventBundleQuery(
    var source: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingCameraEventBundle): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingCameraEventBundle>): MutableList<PendingCameraEventBundle> {
        val matched = filter(items) { candidate: PendingCameraEventBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingCameraEventBundles(store: PendingCameraEventBundleStore, query: PendingCameraEventBundleQuery): MutableList<PendingCameraEventBundle> {
    return query.apply(store.list())
}
