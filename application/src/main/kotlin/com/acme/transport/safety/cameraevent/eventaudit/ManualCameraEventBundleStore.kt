package com.acme.transport.safety.cameraevent.eventaudit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps manual camera event bundles by code. */
interface ManualCameraEventBundleStore {
    fun find(code: String): ManualCameraEventBundle?
    fun save(item: ManualCameraEventBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualCameraEventBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualCameraEventBundleStore(
    var items: MutableMap<String, ManualCameraEventBundle>,
    var order: MutableList<String>,
) : ManualCameraEventBundleStore {
    override fun find(code: String): ManualCameraEventBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualCameraEventBundle) {
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

    override fun list(): MutableList<ManualCameraEventBundle> {
        val out = mutableListOf<ManualCameraEventBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualCameraEventBundleStore(): ManualCameraEventBundleStore {
    return MemoryManualCameraEventBundleStore(
        mutableMapOf<String, ManualCameraEventBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualCameraEventBundleStore(
    var inner: ManualCameraEventBundleStore,
    var cache: TtlCache<ManualCameraEventBundle>,
    var hits: Int,
    var misses: Int,
) : ManualCameraEventBundleStore {
    override fun find(code: String): ManualCameraEventBundle? {
        val hit = (cache.get(code) as ManualCameraEventBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualCameraEventBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualCameraEventBundle> = inner.list()
}

fun newCachedManualCameraEventBundleStore(inner: ManualCameraEventBundleStore, ttl: Int, clock: Clock): ManualCameraEventBundleStore {
    return CachedManualCameraEventBundleStore(
        inner = inner,
        cache = newTtlCache<ManualCameraEventBundle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual camera event bundles. Empty fields match all. */
data class ManualCameraEventBundleQuery(
    var category: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualCameraEventBundle): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualCameraEventBundle>): MutableList<ManualCameraEventBundle> {
        val matched = filter(items) { candidate: ManualCameraEventBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualCameraEventBundles(store: ManualCameraEventBundleStore, query: ManualCameraEventBundleQuery): MutableList<ManualCameraEventBundle> {
    return query.apply(store.list())
}
