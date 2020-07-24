package com.mmdev.pizzatime

import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import com.mmdev.pizzatime.MainActivity.Direction.BACK
import com.mmdev.pizzatime.MainActivity.Direction.FORWARD
import com.mmdev.pizzatime.MainActivity.PizzaSize.*
import com.mmdev.pizzatime.MainActivity.PizzaToppings.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.pizza_views.*


class MainActivity: AppCompatActivity() {

	private enum class Direction { BACK, FORWARD }
	private enum class PizzaSize { S, M, L }
	private enum class PizzaToppings { MUSHROOMS, TOMATOES, BACON, CHEESE, CHILI }

	private data class Pizza(val name: String,
	                         val image: Int,
	                         val price: Int,
	                         var size: PizzaSize = M,
	                         val toppings: MutableList<PizzaToppings> = mutableListOf())



	// size should be >= 5
	private val pizzaList = listOf(
			Pizza("Chef's pizza", R.drawable.pizza_1_firmennaya, 14),
			Pizza("Bavarian", R.drawable.pizza_2_bavarska, 16),
			Pizza("Margherita", R.drawable.pizza_3_margarita, 22),
			Pizza("Meat pizza", R.drawable.pizza_4_myasna, 20),
			Pizza("Village pizza", R.drawable.pizza_5_po_selyanski, 25),
			Pizza("Salami pizza", R.drawable.pizza_6_salyzmi, 20),
			Pizza("Vegetarian", R.drawable.pizza_7_vegetarianska, 19)
	)

	private var pizza1 = pizzaList[0]
	private var pizza2 = pizzaList[1]
	private var pizza3 = pizzaList[2]
	private var pizza4 = pizzaList[3]
	private var pizza5 = pizzaList[4]

	private var currentPizzaInFocus: Pizza = pizzaList[0]

	private var sizeSelected : PizzaSize = M

	private var currentIter = 0
	private var dragDirection: Direction = FORWARD





	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		setupInitialState()

		setupViews()

		motionLayout.addTransitionListener(object : MotionLayout.TransitionListener {
			override fun onTransitionTrigger(motionLayout: MotionLayout, triggerId: Int, positive: Boolean, progress: Float) {}

			// 2131231040 -> 2131231012
			// 2131231012 -> 2131231077
			// 2131231077 -> 2131230881
			// 2131230881 -> 2131230913
			override fun onTransitionStarted(motionLayout: MotionLayout, start: Int, end: Int) {

				//Log.wtf("mylogs", "$start $end")

				when (start) {
					R.id.thirdPos -> when (end) {
						R.id.secondPos -> dragDirection = BACK
						R.id.fourthPos -> dragDirection = FORWARD
					}
				}

			}

			override fun onTransitionChange(motionLayout: MotionLayout, start: Int, end: Int, position: Float) {}

			override fun onTransitionCompleted(motionLayout: MotionLayout, currentId: Int) {

				val findPizzaInFocus = {
					when (currentId) {
						R.id.firstPos -> { currentPizzaInFocus = pizza1 }
						R.id.secondPos -> { currentPizzaInFocus = pizza2 }
						R.id.thirdPos -> { currentPizzaInFocus = pizza3 }
						R.id.fourthPos -> { currentPizzaInFocus = pizza4 }
						R.id.lastPos -> { currentPizzaInFocus = pizza5 }
					}
				}

				when (dragDirection) {
					FORWARD -> {
						// (..-5) because 5 images from list are already used
						// size should be greater or equals than 5
						if (currentId == R.id.fourthPos && currentIter < pizzaList.size - 5) {
							forwardChange(motionLayout)
						}
						else findPizzaInFocus.invoke()
					}
					BACK -> {
						if (currentId == R.id.secondPos && currentIter > 0) {
							backwardChange(motionLayout)
						}
						else findPizzaInFocus.invoke()
					}
				}

				if (currentId !in arrayOf(R.id.pizzaCustomizeState,
				                          R.id.preCustomizerState,
				                          R.id.firstPos)) {
					pizzaName.setText(currentPizzaInFocus.name)
					pizzaPrice.setText("$${currentPizzaInFocus.price}")
				}

			}
		})

	}

	private fun setupInitialState() {
		//init imgs for pizza
		pizzaImg_1.setImageResource(pizzaList[0].image)
		pizzaImg_2.setImageResource(pizzaList[1].image)
		pizzaImg_3.setImageResource(pizzaList[2].image)
		pizzaImg_4.setImageResource(pizzaList[3].image)
		pizzaImg_5.setImageResource(pizzaList[4].image)

		//init first prices and names
		pizzaName.setText(pizzaList[0].name)
		pizzaPrice.setText("$${pizzaList[0].price}")

		//select size by default M
		setSelectedSizeM()

		pizza1 = pizzaList[0]
		pizza2 = pizzaList[1]
		pizza3 = pizzaList[2]
		pizza4 = pizzaList[3]
		pizza5 = pizzaList[4]

		currentPizzaInFocus = pizzaList[0]
		currentIter = 0
		dragDirection = FORWARD

		//clear toppings selection
		btnAddMushrooms.isSelected = false
		btnAddTomato.isSelected = false
		btnAddBacon.isSelected = false
		btnAddCheese.isSelected = false
		btnAddChili.isSelected = false
	}

	private fun setupViews() {
		val initialTranslationX = ivHotIndicator.translationX
		//back to pizzas carousel
		toolbarBackBtn.setOnClickListener {
			motionLayout.setTransition(R.id.backToPizzaCarousel)
			motionLayout.transitionToEnd()
			toolbarBackBtn.isClickable = false
			pizzaViewsBounds.isClickable = true
			setupInitialState()
		}

		//open pizza customizer
		pizzaViewsBounds.setOnClickListener {
			pizzaImg_1.setImageResource(currentPizzaInFocus.image)
			motionLayout.setTransition(R.id.openCustomizer)
			motionLayout.transitionToEnd()
			//prevent to click again on this
			pizzaViewsBounds.isClickable = false
			toolbarBackBtn.isClickable = true
		}

		// pizza name appearance anim
		val inAnimName = AnimationUtils.loadAnimation(this, android.R.anim.fade_in).apply {
			duration = 300
		}
		// pizza name disappearing anim
		val outAnimName = AnimationUtils.loadAnimation(this, android.R.anim.fade_out).apply {
			duration = 300
		}

		//apply anim to textSwitcher
		pizzaName.apply {
			inAnimation = inAnimName
			outAnimation = outAnimName
		}

		// pizza price appearance anim
		val inAnimPrice = AnimationUtils.loadAnimation(this, R.anim.price_in)

		// pizza price disappearing anim
		val outAnimPrice = AnimationUtils.loadAnimation(this, R.anim.price_out)

		pizzaPrice.apply {
			inAnimation = inAnimPrice
			outAnimation = outAnimPrice
		}

		btnSize_S.setOnClickListener { setSelectedSizeS() }
		btnSize_M.setOnClickListener { setSelectedSizeM() }
		btnSize_L.setOnClickListener { setSelectedSizeL() }

		btnAddMushrooms.setOnClickListener {
			with(MUSHROOMS){
				if (it.isSelected) {
					it.isSelected = false
					currentPizzaInFocus.toppings.remove(this)
				}
				else {
					it.isSelected = true
					currentPizzaInFocus.toppings.add(this)
				}
			}
		}

		btnAddTomato.setOnClickListener {
			with(TOMATOES){
				if (it.isSelected) {
					it.isSelected = false
					currentPizzaInFocus.toppings.remove(this)
				}
				else {
					it.isSelected = true
					currentPizzaInFocus.toppings.add(this)
				}
			}
		}

		btnAddBacon.setOnClickListener {
			with(BACON){
				if (it.isSelected) {
					it.isSelected = false
					currentPizzaInFocus.toppings.remove(this)
				}
				else {
					it.isSelected = true
					currentPizzaInFocus.toppings.add(this)
				}
			}
		}

		btnAddCheese.setOnClickListener {
			with(CHEESE){
				if (it.isSelected) {
					it.isSelected = false
					currentPizzaInFocus.toppings.remove(this)
				}
				else {
					it.isSelected = true
					currentPizzaInFocus.toppings.add(this)
				}
			}
		}

		btnAddChili.setOnClickListener {
			with(CHILI){
				if (it.isSelected) {
					it.isSelected = false
					currentPizzaInFocus.toppings.remove(this)
					ivHotIndicator.animate().translationX(initialTranslationX).setDuration(500)
				}
				else {
					it.isSelected = true
					currentPizzaInFocus.toppings.add(this)
					ivHotIndicator.animate().translationX(0f).setDuration(500)
				}
			}
		}
	}

	/**
	 * This method primary use to imitate views loop when swiping forward
	 * Swap between thirdPos and fourthPos
	 * ThirdPos always in center
	 */
	private fun forwardChange(motionLayout: MotionLayout) {
		motionLayout.setTransition(R.id.fourthToThird).also {
			motionLayout.setTransitionDuration(0)
		}
		motionLayout.transitionToEnd()

		currentIter++
		pizzaImg_1.setImageDrawable(pizzaImg_2.drawable)
		pizza1 = pizza2
		pizzaImg_2.setImageDrawable(pizzaImg_3.drawable)
		pizza2 = pizza3
		pizzaImg_3.setImageDrawable(pizzaImg_4.drawable)
		pizza3 = pizza4
		pizzaImg_4.setImageDrawable(pizzaImg_5.drawable)
		pizza4 = pizza5
		// (...+4) because 4 images from list are already used
		pizza5 = pizzaList[currentIter + 4]
		pizzaImg_5.setImageResource(pizzaList[currentIter + 4].image)


		//third pos in focus
		currentPizzaInFocus = pizza3
	}

	/**
	 * This method primary use to imitate views loop when swiping back
	 * Swap between secondPos and thirdPos
	 * ThirdPos always in center
	 */
	private fun backwardChange(motionLayout: MotionLayout) {
		motionLayout.setTransition(R.id.secondToThird).also {
			motionLayout.setTransitionDuration(0)
		}
		motionLayout.transitionToEnd()

		currentIter--
		pizzaImg_5.setImageDrawable(pizzaImg_4.drawable)
		pizza5 = pizza4
		pizzaImg_4.setImageDrawable(pizzaImg_3.drawable)
		pizza4 = pizza3
		pizzaImg_3.setImageDrawable(pizzaImg_2.drawable)
		pizza3 = pizza2
		pizzaImg_2.setImageDrawable(pizzaImg_1.drawable)
		pizza2 = pizza1
		pizzaImg_1.setImageResource(pizzaList[currentIter].image)
		pizza1 = pizzaList[currentIter]


		//third pos in focus
		currentPizzaInFocus = pizza3
	}


	private fun setSelectedSizeS() {
		btnSize_S.isSelected = true
		sizeSelected = S
		btnSize_M.isSelected = false
		btnSize_L.isSelected = false
	}

	private fun setSelectedSizeM() {
		btnSize_S.isSelected = false
		btnSize_M.isSelected = true
		sizeSelected = M
		btnSize_L.isSelected = false
	}

	private fun setSelectedSizeL() {
		btnSize_S.isSelected = false
		btnSize_M.isSelected = false
		btnSize_L.isSelected = true
		sizeSelected = L
	}
}