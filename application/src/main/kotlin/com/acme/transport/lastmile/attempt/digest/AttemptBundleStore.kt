package com.acme.transport.lastmile.attempt.digest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps attempt bundles by code. */
interface AttemptBundleStore {
    fun find(code: String): AttemptBundle?
    fun save(item: AttemptBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<AttemptBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAttemptBundleStore(
    var items: MutableMap<String, AttemptBundle>,
    var order: MutableList<String>,
) : AttemptBundleStore {
    override fun find(code: String): AttemptBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AttemptBundle) {
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

    override fun list(): MutableList<AttemptBundle> {
        val out = mutableListOf<AttemptBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAttemptBundleStore(): AttemptBundleStore {
    return MemoryAttemptBundleStore(mutableMapOf<String, AttemptBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAttemptBundleStore(
    var inner: AttemptBundleStore,
    var cache: TtlCache<AttemptBundle>,
    var hits: Int,
    var misses: Int,
) : AttemptBundleStore {
    override fun find(code: String): AttemptBundle? {
        val hit = (cache.get(code) as AttemptBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AttemptBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AttemptBundle> = inner.list()
}

fun newCachedAttemptBundleStore(inner: AttemptBundleStore, ttl: Int, clock: Clock): AttemptBundleStore {
    return CachedAttemptBundleStore(
        inner = inner,
        cache = newTtlCache<AttemptBundle>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects attempt bundles. Empty fields match all. */
data class AttemptBundleQuery(
    var depot: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AttemptBundle): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AttemptBundle>): MutableList<AttemptBundle> {
        val matched = filter(items) { candidate: AttemptBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAttemptBundles(store: AttemptBundleStore, query: AttemptBundleQuery): MutableList<AttemptBundle> {
    return query.apply(store.list())
}
