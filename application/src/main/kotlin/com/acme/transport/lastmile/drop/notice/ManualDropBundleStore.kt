package com.acme.transport.lastmile.drop.notice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps manual drop bundles by code. */
interface ManualDropBundleStore {
    fun find(code: String): ManualDropBundle?
    fun save(item: ManualDropBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualDropBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualDropBundleStore(
    var items: MutableMap<String, ManualDropBundle>,
    var order: MutableList<String>,
) : ManualDropBundleStore {
    override fun find(code: String): ManualDropBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualDropBundle) {
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

    override fun list(): MutableList<ManualDropBundle> {
        val out = mutableListOf<ManualDropBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualDropBundleStore(): ManualDropBundleStore {
    return MemoryManualDropBundleStore(mutableMapOf<String, ManualDropBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualDropBundleStore(
    var inner: ManualDropBundleStore,
    var cache: TtlCache<ManualDropBundle>,
    var hits: Int,
    var misses: Int,
) : ManualDropBundleStore {
    override fun find(code: String): ManualDropBundle? {
        val hit = (cache.get(code) as ManualDropBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualDropBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualDropBundle> = inner.list()
}

fun newCachedManualDropBundleStore(inner: ManualDropBundleStore, ttl: Int, clock: Clock): ManualDropBundleStore {
    return CachedManualDropBundleStore(
        inner = inner,
        cache = newTtlCache<ManualDropBundle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual drop bundles. Empty fields match all. */
data class ManualDropBundleQuery(
    var name: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualDropBundle): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualDropBundle>): MutableList<ManualDropBundle> {
        val matched = filter(items) { candidate: ManualDropBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualDropBundles(store: ManualDropBundleStore, query: ManualDropBundleQuery): MutableList<ManualDropBundle> {
    return query.apply(store.list())
}
