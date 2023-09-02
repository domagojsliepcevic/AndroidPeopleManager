package hr.algebra.peoplemanager

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.squareup.picasso.Picasso
import hr.algebra.peoplemanager.dao.Person
import hr.algebra.peoplemanager.databinding.FragmentSecondBinding
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
const val PERSON_ID = "hr.algebra.peoplemanager.person_id"
private const val IMAGE_TYPE = "image/*"

class SecondFragment : Fragment() {

    private lateinit var person: Person
    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchPerson()
        setupListeners()




    }

    private fun fetchPerson() {
        val personID = arguments?.getLong(PERSON_ID)
        if (personID != null) {
            GlobalScope.launch(Dispatchers.Main) {
                person =  withContext(Dispatchers.IO) {
                    //IO dretva
                    (context?.applicationContext as App).getPersonDao().getPerson(personID) ?: Person()
                }
                //GUI dretva
                bindPerson()
            }
        }else{
            person = Person()
            bindPerson()
        }
    }

    private fun bindPerson() {

        Picasso.get()
            .load(File(person.picturePath ?: ""))
            .error(R.mipmap.ic_launcher)
            .transform(RoundedCornersTransformation(50,50))
            .into(binding.ivImage)

        binding.tvDate.text = person.birthDate.format(DateTimeFormatter.ISO_DATE)
        binding.etTitle.setText(person.title ?: "")
        binding.etFirstName.setText(person.firstName ?: "")
        binding.etLastName.setText(person.lastName ?: "")
    }

    private fun setupListeners() {
        binding.tvDate.setOnClickListener{
            handleDate()
        }
        binding.ivImage.setOnLongClickListener {
            handleImage()
            true
        }
        binding.btnCommit.setOnClickListener {
            if(formValid()){
                commit()
            }
        }
        binding.etTitle.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                person.title = text?.toString()?.trim()

            }
            override fun afterTextChanged(s: Editable?) {}

        })
        binding.etFirstName.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                person.firstName = text?.toString()?.trim()

            }
            override fun afterTextChanged(s: Editable?) {}

        })
        binding.etLastName.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                person.lastName = text?.toString()?.trim()

            }
            override fun afterTextChanged(s: Editable?) {}

        })
    }

    private fun handleDate() {
        DatePickerDialog(
            requireContext(),
            {_,year,month,dayOfMonth -> person.birthDate = LocalDate.of(year,month + 1,dayOfMonth)
                bindPerson()},
            person.birthDate.year,
            person.birthDate.monthValue - 1,
            person.birthDate.dayOfMonth
        ).show()
    }

    private fun handleImage() {
        Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = IMAGE_TYPE
            imageResult.launch(this)
        }

    }

    private val imageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == Activity.RESULT_OK && it.data != null){
            if(person.picturePath != null){
                File(person.picturePath).delete()
            }
            val dir = context?.applicationContext?.getExternalFilesDir(null)
            val file = File(dir,
                File.separator.toString() + UUID.randomUUID().toString() + ".jpg")

            context?.contentResolver?.openInputStream(it.data?.data  as Uri ).use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    val bos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,bos)
                    outputStream.write(bos.toByteArray())
                    person.picturePath = file.absolutePath
                    bindPerson()
                }
            }
        }

    }

    private fun formValid(): Boolean {
        var ok = true
        arrayOf(binding.etTitle,binding.etFirstName,binding.etLastName).forEach {
            if (it.text.trim().isBlank()){
                it.error = "Please insert"
                ok = false
            }
        }
        return ok && person.picturePath != null
    }

    private fun commit() {
        GlobalScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO){
                //IO dretva
                if (person._id == null){
                (context?.applicationContext as App).getPersonDao().insert(person)
            }else{
                (context?.applicationContext as App).getPersonDao().update(person)
                }}
            //GUI dretva

            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}