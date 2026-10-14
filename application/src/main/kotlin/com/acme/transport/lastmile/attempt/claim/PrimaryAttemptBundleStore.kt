package com.acme.transport.lastmile.attempt.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps primary attempt bundles by code. */
interface PrimaryAttemptBundleStore {
    fun find(code: String): PrimaryAttemptBundle?
    fun save(item: PrimaryAttemptBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryAttemptBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryAttemptBundleStore(
    var items: MutableMap<String, PrimaryAttemptBundle>,
    var order: MutableList<String>,
) : PrimaryAttemptBundleStore {
    override fun find(code: String): PrimaryAttemptBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryAttemptBundle) {
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

    override fun list(): MutableList<PrimaryAttemptBundle> {
        val out = mutableListOf<PrimaryAttemptBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryAttemptBundleStore(): PrimaryAttemptBundleStore {
    return MemoryPrimaryAttemptBundleStore(
        mutableMapOf<String, PrimaryAttemptBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryAttemptBundleStore(
    var inner: PrimaryAttemptBundleStore,
    var cache: TtlCache<PrimaryAttemptBundle>,
    var hits: Int,
    var misses: Int,
) : PrimaryAttemptBundleStore {
    override fun find(code: String): PrimaryAttemptBundle? {
        val hit = (cache.get(code) as PrimaryAttemptBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryAttemptBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryAttemptBundle> = inner.list()
}

fun newCachedPrimaryAttemptBundleStore(inner: PrimaryAttemptBundleStore, ttl: Int, clock: Clock): PrimaryAttemptBundleStore {
    return CachedPrimaryAttemptBundleStore(
        inner = inner,
        cache = newTtlCache<PrimaryAttemptBundle>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary attempt bundles. Empty fields match all. */
data class PrimaryAttemptBundleQuery(
    var title: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryAttemptBundle): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<PrimaryAttemptBundle>): MutableList<PrimaryAttemptBundle> {
        val matched = filter(items) { candidate: PrimaryAttemptBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryAttemptBundles(store: PrimaryAttemptBundleStore, query: PrimaryAttemptBundleQuery): MutableList<PrimaryAttemptBundle> {
    return query.apply(store.list())
}
